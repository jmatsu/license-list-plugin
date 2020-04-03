package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.ext.xor2
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.MergerableAssembler
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

abstract class MergeLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
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
            variantScope = args.variantScope,
            additionalScopes = args.additionalScopes
        )
        val disassembler = Disassembler(
            style = args.assemblyStyle,
            format = args.assemblyFormat
        )

        val artifactsText = args.assembledArtifactsFile.readText()
        val catalogText = args.assembledLicenseCatalogFile.readText()

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
            style = args.assemblyStyle,
            format = args.assemblyFormat
        )
        val licenseCatalogText = assembler.assemblePlainLicenses(Convention.Yaml.Assembly) // the format is fixed

        args.assembledArtifactsFile.writeText(newArtifactsText)
        args.assembledLicenseCatalogFile.writeText(licenseCatalogText)
    }

    class Args(
        project: Project,
        extension: LicenseListExtension,
        variant: ApplicationVariant
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = variant
    )
}
