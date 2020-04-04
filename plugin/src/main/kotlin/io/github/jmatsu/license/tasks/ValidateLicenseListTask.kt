package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.ext.xor2
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.TaskException
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import java.io.FileNotFoundException
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

abstract class ValidateLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
) : VariantAwareTask(extension, variant) {
    class InvalidLicenseException(message: String) : TaskException(message)

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        if (!args.assembledArtifactsFile.exists()) {
            throw FileNotFoundException("${args.assembledArtifactsFile.absolutePath} is not found")
        }

        if (!args.assembledLicenseCatalogFile.exists()) {
            throw FileNotFoundException("${args.assembledLicenseCatalogFile.absolutePath} is not found")
        }

        val artifactIgnoreParser = ArtifactIgnoreParser(
            ignoreFile = args.ignoreFile
        )

        val artifactManagement = ArtifactManagement(
            project = project,
            configurationNames = args.configurationNames,
            exclusionRegex = artifactIgnoreParser.parse()
        )
        val scopedResolvedArtifacts = artifactManagement.analyze(
            variantScope = args.variantScope,
            additionalScopes = args.additionalScopes
        )

        val assembler = Assembler(
            resolvedArtifactMap = scopedResolvedArtifacts
        )
        val disassembler = Disassembler(
            style = args.assemblyStyle,
            format = args.assemblyFormat
        )

        val artifactsText = args.assembledArtifactsFile.readText()
        val catalogText = args.assembledLicenseCatalogFile.readText()

        val currentArtifacts = assembler.transformForFlatten()

        val recordedArtifactKeys = disassembler.disassembleArtifacts(artifactsText).map { it.key }
        val currentArtifactKeys = currentArtifacts.map { it.key }

        val (addedArtifactKeys, removedArtifactKeys) = currentArtifactKeys.xor2(recordedArtifactKeys)

        val recordedLicenseKeys = disassembler.disassemblePlainLicenses(catalogText).map { it.key }
        val currentLicenseKeys = currentArtifacts.flatMap { it.licenses }

        val (addedLicenseKeys, removedLicenseKeys) = currentLicenseKeys.xor2(recordedLicenseKeys)

        if (removedArtifactKeys.isNotEmpty() || removedLicenseKeys.isNotEmpty()) {
            logger.warn("You can remove the following artifacts and licenses.\n")

            logger.warn("--- artifacts ---")

            removedArtifactKeys.forEach { key ->
                logger.warn(key)
            }

            logger.warn("--- licenses ---")

            removedLicenseKeys.forEach { key ->
                logger.warn(key.value)
            }

            logger.warn("\n")
        }

        if (addedArtifactKeys.isNotEmpty() || addedLicenseKeys.isNotEmpty()) {
            logger.warn("You need to handle the following that the current license file does not contain.\n")

            logger.warn("--- artifacts ---")

            addedArtifactKeys.forEach { key ->
                logger.warn(key)
            }

            logger.warn("--- licenses ---")

            addedLicenseKeys.forEach { key ->
                logger.warn(key.value)
            }

            throw InvalidLicenseException(
                "${addedArtifactKeys.size} artifacts and ${addedLicenseKeys.size} licenses must be added"
            )
        }
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
