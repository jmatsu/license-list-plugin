package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Convention
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.TaskException
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

abstract class InitLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
) : VariantAwareTask(extension, variant) {
    class FileAlreadyExistException(message: String) : TaskException(message)

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        if (args.assembledArtifactsFile.exists() && !args.forceOverwrite) {
            throw FileAlreadyExistException("Overwriting ${args.assembledArtifactsFile.absolutePath} is forbidden. Provide overwrite=true when running this task to overwrite.")
        }

        if (args.assembledLicenseCatalogFile.exists() && !args.forceOverwrite) {
            throw FileAlreadyExistException("Overwriting ${args.assembledArtifactsFile.absolutePath} is forbidden. Provide overwrite=true when running this task to overwrite.")
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

        val artifactsText = assembler.assembleArtifacts(args.assemblyStyle, args.assemblyFormat)
        val licenseCatalogText = assembler.assemblePlainLicenses(Convention.Yaml.Assembly) // the format is fixed

        args.assembleOutputDir.mkdirs()
        args.assembledArtifactsFile.writeText(artifactsText)
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
    ) {
        val forceOverwrite: Boolean = project.properties["overwrite"]?.toString() == "true"
    }
}
