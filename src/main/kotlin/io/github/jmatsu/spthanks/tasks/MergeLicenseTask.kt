package io.github.jmatsu.spthanks.tasks

import com.android.build.gradle.api.ApplicationVariant
import io.github.jmatsu.spthanks.SpecialThanksExtension
import io.github.jmatsu.spthanks.ext.collectToMap
import io.github.jmatsu.spthanks.internal.ArtifactManagement
import io.github.jmatsu.spthanks.poko.ArtifactDefinition
import io.github.jmatsu.spthanks.poko.PlainLicense
import io.github.jmatsu.spthanks.poko.Scope
import io.github.jmatsu.spthanks.presentation.Assembler
import io.github.jmatsu.spthanks.presentation.Disassembler
import io.github.jmatsu.spthanks.tasks.internal.ReadWriteLicenseTaskArgs
import io.github.jmatsu.spthanks.tasks.internal.VariantAwareTask
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ChangeListener
import org.gradle.util.DiffUtil

abstract class MergeLicenseTask
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
        val licenseCapture = HashSet<PlainLicense>()
        val scopedArtifactDefinitions = scopedResolvedArtifacts.mapValues { (_, artifacts) ->
            artifacts.map { Assembler.assembleArtifact(it, licenseCapture) }
        }

        val disassembler = Disassembler(
                style = args.style,
                format = args.format
        )

        val text = args.artifactsFile.readText()

        val recordedArtifacts = disassembler.disassemble(text).toSet()
        val currentArtifacts = scopedArtifactDefinitions.values.flatten().toSet()

        val newArtifacts: MutableList<ArtifactDefinition> = ArrayList()
        val removedArtifacts: MutableList<ArtifactDefinition> = ArrayList()

        // FIXME may be better to use Myers algorithm because of time-complexity and interfaces
        DiffUtil.diff(currentArtifacts.groupBy { it.key }, recordedArtifacts.groupBy { it.key }, object : ChangeListener<Map.Entry<String, List<ArtifactDefinition>>> {
            override fun added(element: Map.Entry<String, List<ArtifactDefinition>>) {
                // TODO skip logic
                newArtifacts += element.value.first()
            }

            override fun changed(element: Map.Entry<String, List<ArtifactDefinition>>) {
//                val new = element.value.first()
//                val recorded = recordedArtifacts.first { it.key == element.key }

                // FIXME determine how this plugin should treat this case
            }

            override fun removed(element: Map.Entry<String, List<ArtifactDefinition>>) {
                removedArtifacts += element.value.first()
            }
        })

        val newText = when (args.style) {
            Assembler.Style.Flatten -> {
                val removedKeys = removedArtifacts.map { it.key }

                Assembler.assembleFlatten(
                        format = args.format,
                        definitions = (recordedArtifacts + newArtifacts).filterNot { it.key in removedKeys }.sorted()
                )
            }
            Assembler.Style.StructuredWithoutScope -> {
                val newKeys = newArtifacts.map { it.key }

                val assemblee = scopedArtifactDefinitions
                        .map { (_, definitions) ->
                            definitions.mergeAndSort(newKeys = newKeys, strongerDefinitions = recordedArtifacts)
                        }.reduce { acc, map -> acc + map }.toSortedMap()

                Assembler.assembleStructuredWithoutScope(
                        format = args.format,
                        definitionMap = assemblee
                )
            }
            Assembler.Style.StructuredWithScope -> {

                val newKeys = newArtifacts.map { it.key }

                val assemblee = scopedArtifactDefinitions
                        .map { (scope, definitions) ->
                            Scope(scope.name) to definitions.mergeAndSort(newKeys = newKeys, strongerDefinitions = recordedArtifacts)
                        }.toMap()

                Assembler.assembleStructuredWithScope(
                        format = args.format,
                        scopedDefinitionMap = assemblee
                )
            }
        }

        // TODO license-catalog
        args.artifactsFile.writeText(newText)
    }

    fun List<ArtifactDefinition>.mergeAndSort(newKeys: List<String>, strongerDefinitions: Set<ArtifactDefinition>): Map<String, List<ArtifactDefinition>> {
        return map { definition ->
            val merged = if (definition.key in newKeys) {
                definition
            } else {
                strongerDefinitions.first { it.key == definition.key }
            }

            // destructure group:name
            merged.key.split(":")[0] to merged.copy(key = merged.key.split(":")[1])
        }.sortedBy { it.second }.sortedBy { it.first }.collectToMap()
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
