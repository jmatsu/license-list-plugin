package io.github.jmatsu.license.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.license.LicenseListExtension
import io.github.jmatsu.license.internal.ArtifactIgnoreParser
import io.github.jmatsu.license.internal.ArtifactManagement
import io.github.jmatsu.license.model.ResolveScope
import io.github.jmatsu.license.model.ResolvedArtifact
import io.github.jmatsu.license.presentation.AssembleeData
import io.github.jmatsu.license.presentation.Assembler
import io.github.jmatsu.license.presentation.Builder
import io.github.jmatsu.license.presentation.Convention
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import java.util.SortedMap
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.StringFormat
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.testfixtures.ProjectBuilder

class InitLicenseListTaskTest {
    lateinit var project: Project
    lateinit var extension: LicenseListExtension

    @BeforeTest
    fun setup() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.jmatsu.license-list")
        extension = requireNotNull(project.extensions.findByType(LicenseListExtension::class))
    }

    @Test
    fun `the task is generate-able`() {
        val variant = mockk<ApplicationVariant>()

        val task = project.tasks.create("sample", InitLicenseListTask::class.java, extension, variant)

        assertTrue(task.isEnabled)
    }

    @Test
    fun `verify method calls`() {
        mockkConstructor(ArtifactIgnoreParser::class, ArtifactManagement::class, Builder::class, Assembler::class)
        mockkStatic("kotlin.io.FilesKt__FileReadWriteKt")

        val additionalScopes: Set<ResolveScope.Addition> = mockk()
        val variantScope: ResolveScope.Variant = mockk()
        val assemblyFormat: StringFormat = mockk()
        val assemblyStyle: Assembler.Style = mockk()

        val args: InitLicenseListTask.Args = mockk {
            every { assembleOutputDir } returns mockk {
                every { mkdirs() } returns true
            }
            every { assembledArtifactsFile.exists() } returns false
            every { assembledLicenseCatalogFile.exists() } returns false
            every { this@mockk.additionalScopes } returns additionalScopes
            every { this@mockk.variantScope } returns variantScope
            every { this@mockk.assemblyFormat } returns assemblyFormat
            every { this@mockk.assemblyStyle } returns assemblyStyle
            every { configurationNames } returns setOf()
            every { ignoreFile } returns mockk()
        }

        val regex: Regex = mockk()
        val analyzedResult: SortedMap<ResolveScope, List<ResolvedArtifact>> = mockk()
        val buildResult = AssembleeData(scopedArtifacts = emptyMap(), licenses = emptyList())
        val assembledArtifacts = "assembledArtifacts"
        val assembledLicenses = "assembledLicenses"

        every {
            anyConstructed<ArtifactIgnoreParser>().parse()
        } returns regex

        every {
            anyConstructed<ArtifactManagement>().analyze(
                additionalScopes = any(),
                variantScope = any()
            )
        } returns analyzedResult

        every {
            anyConstructed<Assembler>().assembleArtifacts(
                format = any(),
                style = any()
            )
        } returns assembledArtifacts

        every {
            anyConstructed<Builder>().build()
        } returns buildResult

        every {
            anyConstructed<Assembler>().assemblePlainLicenses(
                format = any()
            )
        } returns assembledLicenses

        every { args.assembledArtifactsFile.writeText(any()) } just (Runs)
        every { args.assembledLicenseCatalogFile.writeText(any()) } just (Runs)

        InitLicenseListTask.Executor(
            project = project,
            args = args
        )

        verify {
            anyConstructed<ArtifactIgnoreParser>().parse()
            anyConstructed<ArtifactManagement>().analyze(
                additionalScopes = additionalScopes,
                variantScope = variantScope
            )
            anyConstructed<Builder>().build()
            anyConstructed<Assembler>().assembleArtifacts(
                format = assemblyFormat,
                style = assemblyStyle
            )
            anyConstructed<Assembler>().assemblePlainLicenses(
                format = Convention.Yaml.Assembly
            )

            args.assembledArtifactsFile.writeText(assembledArtifacts)
            args.assembledLicenseCatalogFile.writeText(assembledLicenses)
        }

        unmockkAll()
    }
}
