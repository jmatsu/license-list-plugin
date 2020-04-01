package io.github.jmatsu.spthanks.model

import org.gradle.api.artifacts.component.ComponentArtifactIdentifier

data class ResolvedModuleIdentifier(
    val group: String,
    val name: String,
    val version: VersionString = VersionString("+"),
    val id: ComponentArtifactIdentifier? = null
) : Comparable<ResolvedModuleIdentifier> {
    override fun compareTo(other: ResolvedModuleIdentifier): Int {
        return "$group:$name".compareTo("${other.group}:${other.name}")
            .takeIf { it != 0 } ?: version.compareTo(other.version)
    }
}
