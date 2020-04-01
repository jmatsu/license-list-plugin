package io.github.jmatsu.license.tasks

import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.poko.DisplayArtifact
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.tasks.internal.BaseTask
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

abstract class CreateLicenseListViewerTask
@Inject constructor(
    extension: LicenseListExtension
) : BaseTask(extension) {

    @TaskAction
    fun execute() {
        val args = Args(project, extension)

        val disassembler = Disassembler(
            style = args.style,
            format = args.format
        )

        val artifactsText = args.artifactsFile.readText()
        val catalogText = args.catalogFile.readText()

        val recordedArtifacts = disassembler.disassembleArtifacts(artifactsText).toSet()
        val recordedLicenses = disassembler.disassemblePlainLicenses(catalogText).toSet()

        val displayArtifacts = recordedArtifacts.map { artifact ->
            DisplayArtifact(
                key = artifact.key,
                displayName = artifact.displayName,
                url = artifact.url,
                copyrightHolders = artifact.copyrightHolders,
                licenses = artifact.licenses.map { key ->
                    recordedLicenses.first { it.key == key }
                }
            )
        }

        // TODO HTML? ListView?
    }

    class Args(
        project: Project,
        extension: LicenseListExtension
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = null
    )
}
