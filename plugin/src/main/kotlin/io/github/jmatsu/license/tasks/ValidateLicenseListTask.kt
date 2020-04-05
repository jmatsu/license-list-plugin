package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.google.common.annotations.VisibleForTesting
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Diff
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.TaskException
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import java.io.FileNotFoundException
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

abstract class ValidateLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
) : VariantAwareTask(extension, variant) {
    class InvalidLicenseException(message: String) : TaskException(message)

    @VisibleForTesting
    internal object Executor {
        operator fun invoke(project: Project, args: Args, logger: Logger) {
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
            val recordedArtifacts = disassembler.disassembleArtifacts(artifactsText).flatMap { (_, xs) -> xs }

            val artifactDiff = Diff.calculateForArtifact(recordedArtifacts, newer = currentArtifacts)

            val currentLicenseKeys = currentArtifacts.flatMap { it.licenses }
            val recordedLicenseKeys = disassembler.disassemblePlainLicenses(catalogText).map { it.key }

            val licenseKeyDiff = Diff.calculateForLicense(recordedLicenseKeys, newer = currentLicenseKeys)

            logger.warn("You can remove the following artifacts and licenses.\n")

            if (artifactDiff.willBeRemovedKeys.isNotEmpty() || licenseKeyDiff.willBeRemovedKeys.isNotEmpty()) {
                logger.warn("--- artifacts ---")

                artifactDiff.willBeRemovedKeys.forEach { key ->
                    logger.warn(key)
                }

                logger.warn("--- licenses ---")

                licenseKeyDiff.willBeRemovedKeys.forEach { key ->
                    logger.warn(key)
                }

                logger.warn("")
            }

            logger.warn("The following artifacts will be kept.\n")

            artifactDiff.keepKeys.forEach { key ->
                logger.warn(key)
            }

            logger.warn("You need to handle the following that the current license file does not contain.\n")

            if (artifactDiff.missingKeys.isNotEmpty() || licenseKeyDiff.missingKeys.isNotEmpty()) {
                logger.warn("--- artifacts ---")

                artifactDiff.missingKeys.forEach { key ->
                    logger.warn(key)
                }

                logger.warn("--- licenses ---")

                licenseKeyDiff.missingKeys.forEach { key ->
                    logger.warn(key)
                }
            }

            if (artifactDiff.hasDiff() || licenseKeyDiff.hasDiff()) {
                fun Diff.DiffResult.toText(label: String) = "${missingKeys.size} ${label}s are missing and ${willBeRemovedKeys.size} ${label}s can be removed."

                throw InvalidLicenseException(
                    arrayOf(
                        artifactDiff.toText("artifact"),
                        licenseKeyDiff.toText("license")
                    ).joinToString(" ")
                )
            }
        }
    }

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        Executor(
            project = project,
            args = args,
            logger = logger
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
