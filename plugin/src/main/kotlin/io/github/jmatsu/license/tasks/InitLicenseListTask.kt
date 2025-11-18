package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Builder
import io.github.jmatsu.license.presentation.Convention
import io.github.jmatsu.license.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.license.tasks.internal.TaskException
import io.github.jmatsu.license.tasks.internal.VariantAwareTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.VisibleForTesting

abstract class InitLicenseListTask
@Inject constructor(
    extension: LicenseListExtension,
    variant: ApplicationVariant
) : VariantAwareTask(extension, variant) {
    class FileAlreadyExistException(message: String) : TaskException(message)

    @VisibleForTesting
    internal object Executor {
        operator fun invoke(project: Project, args: Args) {
            if (args.assembledArtifactsFile.exists() && !args.forceOverwrite) {
                throw FileAlreadyExistException("Overwriting ${args.assembledArtifactsFile.absolutePath} is forbidden. Provide overwrite=true when running this task to overwrite.")
            }

            if (args.assembledLicenseCatalogFile.exists() && !args.forceOverwrite) {
                throw FileAlreadyExistException("Overwriting ${args.assembledArtifactsFile.absolutePath} is forbidden. Provide overwrite=true when running this task to overwrite.")
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
            val builder = Builder(
                resolvedArtifactMap = scopedResolvedArtifacts
            )
            val assembler = Assembler(
                assembleeData = builder.build()
            )

            val artifactsText = assembler.assembleArtifacts(args.assemblyStyle, args.assemblyFormat)
            val licenseCatalogText = assembler.assemblePlainLicenses(Convention.Yaml.Assembly) // the format is fixed

            args.assembleOutputDir.mkdirs()
            args.assembledArtifactsFile.writeText(artifactsText)
            args.assembledLicenseCatalogFile.writeText(licenseCatalogText)
        }
    }

    @TaskAction
    fun execute() {
        val args = Args(project, extension, variant)

        Executor(
            project = project,
            args = args
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
    ) {
        val forceOverwrite: Boolean = project.properties["overwrite"]?.toString() == "true"
    }
}
