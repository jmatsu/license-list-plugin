package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.presentation.Disassembler
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import java.io.FileNotFoundException
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ChangeListener
import org.gradle.util.DiffUtil

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

        val disassembler = Disassembler(
            style = args.style,
            format = args.format
        )

        val text = args.artifactsFile.readText()

        val recordedArtifacts = disassembler.disassemble(text).map { it.key }.toSet()
        val currentArtifacts = scopedResolvedArtifacts.flatMap { (_, artifacts) -> artifacts.map { "${it.id.group}:${it.id.name}" } }.toSet()

        val addedKeys = ArrayList<String>()
        val removedKeys = ArrayList<String>()

        DiffUtil.diff(currentArtifacts, recordedArtifacts, object : ChangeListener<String> {
            override fun added(element: String) {
                // TODO implement skip logic?
                addedKeys += element
            }

            override fun changed(element: String) {
                error("DiffUtil does not support changed because it's based on Set")
            }

            override fun removed(element: String) {
                removedKeys += element
            }
        })

        if (removedKeys.isNotEmpty()) {
            logger.warn("You can remove the following artifacts.\n")

            removedKeys.forEach { key ->
                logger.warn(key)
            }

            logger.warn("\n")
        }

        if (addedKeys.isNotEmpty()) {
            logger.warn("You need to handle the following artifacts that the current license file does not contain.\n")

            addedKeys.forEach { key ->
                logger.warn(key)
            }

            throw InvalidLicenseException(
                "${addedKeys.size} artifacts needs to be added"
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
