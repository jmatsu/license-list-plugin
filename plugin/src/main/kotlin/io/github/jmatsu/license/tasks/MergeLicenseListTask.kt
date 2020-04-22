package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.google.common.annotations.VisibleForTesting
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Merger
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

    @VisibleForTesting
    internal object Executor {
        operator fun invoke(project: Project, args: Args) {
            val artifactIgnoreParser = ArtifactIgnoreParser(
                ignoreFile = args.ignoreFile
            )

            val artifactManagement = ArtifactManagement(
                project = project,
                configurationNames = args.configurationNames,
                exclusionPredicate = artifactIgnoreParser.buildPredicate(args.ignoreFormat)
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

            val scopedBaseArtifacts = disassembler.disassembleArtifacts(artifactsText)
            val recordedLicenses = disassembler.disassemblePlainLicenses(catalogText).toSet()

            val merger = Merger(
                scopedResolvedArtifacts = scopedResolvedArtifacts,
                scopedBaseArtifacts = scopedBaseArtifacts,
                baseLicenses = recordedLicenses
            )

            val assembler = Assembler(
                assembleeData = merger.merge()
            )

            val newArtifactsText = assembler.assembleArtifacts(
                style = args.assemblyStyle,
                format = args.assemblyFormat
            )
            val licenseCatalogText = assembler.assemblePlainLicenses(Convention.Yaml.Assembly) // the format is fixed

            args.assembledArtifactsFile.writeText(newArtifactsText)
            args.assembledLicenseCatalogFile.writeText(licenseCatalogText)
        }
    }

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        Executor(
            project = project,
            args = args
        )
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
