package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.ext.xor2
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.poko.PlainLicense
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.presentation.Convention
import io.github.jmatsu.spthanks.presentation.Disassembler
import io.github.jmatsu.spthanks.presentation.MergerableAssembler
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class MergeLicenseTask
@Inject constructor(
    extension: SpecialThanksExtension,
    variant: ApplicationVariant?
) : VariantAwareTask(extension, variant) {

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        val artifactManagement = ArtifactManagement(
            project = project,
            configurationNames = args.configurationNames,
            excludeGroups = args.excludeGroups,
            excludeArtifacts = args.excludeArtifacts
        )
        val scopedResolvedArtifacts = artifactManagement.analyze(
            variantScopes = args.variantScopes,
            additionalScopes = args.additionalScopes
        )
        val disassembler = Disassembler(
            style = args.style,
            format = args.format
        )

        val artifactsText = args.artifactsFile.readText()
        val catalogText = args.catalogFile.readText()

        val recordedArtifacts = disassembler.disassembleArtifacts(artifactsText).toSet()
        val recordedLicenses = disassembler.disassemblePlainLicenses(catalogText).toSet()

        val currentArtifacts = run {
            val fake = HashSet<PlainLicense>()

            scopedResolvedArtifacts.flatMap { (_, artifacts) ->
                artifacts.map { Assembler.assembleArtifact(it, licenseCapture = fake) }
            }
        }

        // TODO support changed artifacts : what's the usecase?
        val (newArtifacts, _, removedArtifacts) = currentArtifacts.xor2(recordedArtifacts) { it.key }

        val assembler = MergerableAssembler(
            scopedResolvedArtifacts = scopedResolvedArtifacts,
            baseArtifacts = recordedArtifacts,
            newArtifacts = newArtifacts,
            removedArtifacts = removedArtifacts,
            baseLicenses = recordedLicenses
        )

        val newArtifactsText = assembler.assembleArtifacts(
            style = args.style,
            format = args.format
        )
        val licenseCatalogText = assembler.assemblePlainLicenses(Convention.Yaml) // the format is fixed

        args.artifactsFile.writeText(newArtifactsText)
        args.catalogFile.writeText(licenseCatalogText)
    }

    class Args(
        project: Project,
        extension: SpecialThanksExtension,
        variant: ApplicationVariant?
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = variant
    )
}
