package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.ext.xor2
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.presentation.Disassembler
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.TaskException
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.FileNotFoundException
import javax.inject.Inject

abstract class ValidateLicenseTask
@Inject constructor(
    extension: SpecialThanksExtension,
    variant: ApplicationVariant?
) : VariantAwareTask(extension, variant) {
    class InvalidLicenseException(message: String) : TaskException(message)

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        if (!args.artifactsFile.exists()) {
            throw FileNotFoundException("${args.artifactsFile.absolutePath} is not found")
        }

        if (!args.catalogFile.exists()) {
            throw FileNotFoundException("${args.catalogFile.absolutePath} is not found")
        }

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

        val assembler = Assembler(
            resolvedArtifactMap = scopedResolvedArtifacts
        )
        val disassembler = Disassembler(
            style = args.style,
            format = args.format
        )

        val artifactsText = args.artifactsFile.readText()
        val catalogText = args.catalogFile.readText()

        val currentArtifacts = assembler.transformForFlatten()

        val recordedArtifactKeys = disassembler.disassembleArtifacts(artifactsText).map { it.key }
        val currentArtifactKeys = currentArtifacts.map { it.key }

        val (addedArtifactKeys, removedArtifactKeys) = currentArtifactKeys.xor2(recordedArtifactKeys)

        val recordedLicenseKeys = disassembler.disassemblePlainLicenses(catalogText).map { it.key }
        val currentLicenseKeys = currentArtifacts.flatMap { it.licenses }.map { it.value }

        val (addedLicenseKeys, removedLicenseKeys) = currentLicenseKeys.xor2(recordedLicenseKeys)

        if (removedArtifactKeys.isNotEmpty() || removedLicenseKeys.isNotEmpty()) {
            logger.warn("You can remove the following artifacts and licenses.\n")

            logger.warn("--- artifacts ---")

            removedArtifactKeys.forEach { key ->
                logger.warn(key)
            }

            logger.warn("--- licenses ---")

            removedLicenseKeys.forEach { key ->
                logger.warn(key)
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
                logger.warn(key)
            }

            throw InvalidLicenseException(
                "${addedArtifactKeys.size} artifacts and ${addedLicenseKeys.size} licenses must be added"
            )
        }
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
