package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.google.common.annotations.VisibleForTesting
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.presentation.Diff
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Merger
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

            val currentData = merger.merge()

            val recordedArtifacts = disassembler.disassembleArtifacts(artifactsText).values.flatten()
            val currentArtifacts = currentData.scopedArtifacts.values.flatten()

            val artifactDiff = Diff.calculateForArtifact(recordedArtifacts, newer = currentArtifacts)

            val currentLicenseKeys = currentArtifacts.flatMap { it.licenses }
            val recordedLicenseKeys = currentData.licenses.map { it.key }

            val licenseKeyDiff = Diff.calculateForLicense(recordedLicenseKeys, newer = currentLicenseKeys)

            if (artifactDiff.willBeRemovedKeys.isNotEmpty() || licenseKeyDiff.willBeRemovedKeys.isNotEmpty()) {
                logger.warn("Extra Keys: You need to remove ${artifactDiff.willBeRemovedKeys.size} artifact keys / ${licenseKeyDiff.willBeRemovedKeys.size} license keys\n")

                logger.warn("--- artifacts ---")

                if (artifactDiff.willBeRemovedKeys.isNotEmpty()) {
                    artifactDiff.willBeRemovedKeys.forEach { key ->
                        logger.warn("\t$key")
                    }
                } else {
                    logger.warn("\tNo Extra Key")
                }

                logger.warn("--- licenses ---")

                if (licenseKeyDiff.willBeRemovedKeys.isNotEmpty()) {
                    licenseKeyDiff.willBeRemovedKeys.forEach { key ->
                        logger.warn("\t$key")
                    }
                } else {
                    logger.warn("\tNo Extra Key")
                }

                logger.warn("")
            } else {
                logger.warn("Extra Keys: PASSED")
            }

            if (artifactDiff.keepKeys.isNotEmpty()) {
                logger.warn("Kept Keys: ${artifactDiff.keepKeys.size} artifacts are found.")
                artifactDiff.keepKeys.forEach { key ->
                    logger.warn(key)
                }

                logger.warn("")
            } else {
                logger.warn("Kept Keys: 0 artifact is found.")
            }

            if (artifactDiff.missingKeys.isNotEmpty() || licenseKeyDiff.missingKeys.isNotEmpty()) {
                logger.warn("Missing Keys: The current artifact/license files don't contain them so you need to manage or add the following artifacts to ignore.\n")

                logger.warn("--- artifacts ---")

                if (artifactDiff.missingKeys.isNotEmpty()) {
                    artifactDiff.missingKeys.forEach { key ->
                        logger.warn("\t$key")
                    }
                } else {
                    logger.warn("\tNo Kissing Key")
                }

                logger.warn("--- licenses ---")

                if (licenseKeyDiff.missingKeys.isNotEmpty()) {
                    licenseKeyDiff.missingKeys.forEach { key ->
                        logger.warn("\t$key")
                    }
                } else {
                    logger.warn("\tNo Kissing Key")
                }
            } else {
                logger.warn("Missing Keys: PASSED")
            }

            if (artifactDiff.hasDiff() || licenseKeyDiff.hasDiff()) {
                logger.warn("Conclusion: FAILED")

                fun Diff.DiffResult.toText(label: String) = "${missingKeys.size} ${label}s are missing and ${willBeRemovedKeys.size} ${label}s can be removed."

                throw InvalidLicenseException(
                    arrayOf(
                        artifactDiff.toText("artifact"),
                        licenseKeyDiff.toText("license")
                    ).joinToString(" ")
                )
            } else {
                logger.warn("Conclusion: PASSED")
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
