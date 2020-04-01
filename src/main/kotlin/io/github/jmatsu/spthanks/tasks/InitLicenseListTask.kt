package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.presentation.Convention
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

abstract class InitLicenseListTask
@Inject constructor(
    extension: SpecialThanksExtension,
    variant: ApplicationVariant?
) : VariantAwareTask(extension, variant) {
    class FileAlreadyExistException(message: String) : TaskException(message)

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        if (args.artifactsFile.exists() && !args.forceOverwrite) {
            throw FileAlreadyExistException("Overwriting ${args.artifactsFile.absolutePath} is forbidden. Please remove the file or provide a property (overwrite=true) when running this task to overwrite.")
        }

        if (args.catalogFile.exists() && !args.forceOverwrite) {
            throw FileAlreadyExistException("Overwriting ${args.catalogFile.absolutePath} is forbidden. Please remove the file or provide a property (overwrite=true) when running this task to overwrite.")
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

        val artifactsText = assembler.assembleArtifacts(args.style, args.format)
        val licenseCatalog = assembler.assemblePlainLicenses(Convention.Yaml) // the format is fixed

        args.outputDir.mkdirs()
        args.artifactsFile.writeText(artifactsText)
        args.catalogFile.writeText(licenseCatalog)
    }

    class Args(
        project: Project,
        extension: SpecialThanksExtension,
        variant: ApplicationVariant?
    ) : ReadWriteLicenseTaskArgs(
            project = project,
            extension = extension,
            variant = variant
    ) {
        val forceOverwrite: Boolean = project.properties["overwrite"]?.toString() == "true"
    }
}
