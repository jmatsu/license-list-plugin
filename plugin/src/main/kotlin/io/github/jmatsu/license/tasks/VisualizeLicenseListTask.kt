package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.poko.DisplayArtifact
import io.github.jmatsu.license.presentation.Disassembler
import io.github.jmatsu.license.presentation.Visualizer
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

abstract class VisualizeLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
) : VariantAwareTask(extension, variant) {

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        val disassembler = Disassembler(
            style = args.style,
            format = args.format
        )

        val artifactsText = args.assembledArtifactsFile.readText()
        val catalogText = args.assembledLicenseCatalogFile.readText()

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

        val visualizer = Visualizer(
            displayArtifacts = displayArtifacts
        )

        val text = visualizer.visualizeArtifacts(Visualizer.Style.JsonStyle)

        args.visualizeOutputDir.mkdirs()
        File(args.visualizeOutputDir, "license.json").writeText(text)

        // TODO HTML? ListView?
    }

    class Args(
        project: Project,
        extension: LicenseListExtension,
        variant: ApplicationVariant
    ) : ReadWriteLicenseTaskArgs(
        project = project,
        extension = extension,
        variant = variant
    ) {
        // FIXME use extension
        val visualizeOutputDir: File =
            variant.sourceSets.flatMap {
                it.assetsDirectories
            }.firstOrNull {
                it.absolutePath.endsWith("/${variant.mergedFlavor.name}/assets")
            } ?: assembleOutputDir
    }
}
