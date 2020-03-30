package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.presentation.Disassembler
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ChangeListener
import org.gradle.util.DiffUtil
import javax.inject.Inject

abstract class ValidateLicenseTask
@Inject constructor(
        extension: SpecialThanksExtension,
        variant: ApplicationVariant?
) : VariantAwareTask(extension, variant) {
    class InvalidLicenseFoundException(message: String) : TaskException(message)

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

        val text = args.licenseFile.readText()

        val recordedArtifacts = disassembler.disassemble(text).map { it.key }.toSet()
        val currentArtifacts = scopedResolvedArtifacts.flatMap { (_, artifacts) -> artifacts.map { "${it.id.group}:${it.id.name}" } }.toSet()

        var newCount = 0
        var removedCount = 0

        DiffUtil.diff(currentArtifacts, recordedArtifacts, object : ChangeListener<String> {
            override fun added(element: String) {
                // TODO skip logic
                newCount++
            }

            override fun changed(element: String) {
                error("DiffUtil does not support changed because it's based on Set")
            }

            override fun removed(element: String) {
                removedCount++
            }
        })

        if (newCount > 0) {
            throw InvalidLicenseFoundException(
                    "$newCount artifacts needs to be mentioned and $removedCount artifacts have gone"
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