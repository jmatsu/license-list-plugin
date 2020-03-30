package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class CreateLicenseListTask
@Inject constructor(
        extension: SpecialThanksExtension,
        variant: ApplicationVariant?
) : VariantAwareTask(extension, variant) {

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
        val assembler = Assembler(
                resolvedArtifactMap = scopedResolvedArtifacts
        )

        val text = assembler.assemble(args.style, args.format)

        args.licenseFile.writeText(text)
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