package io.github.jmatsu.license.model

import org.gradle.api.artifacts.component.ComponentArtifactIdentifier

data class ResolvedModuleIdentifier(
    val group: String,
    val name: String,
    val version: VersionString = VersionString("+"),
    /**
     * Optional. Present iff resolved artifact
     */
    val id: ComponentArtifactIdentifier? = null,
) : Comparable<ResolvedModuleIdentifier> {
    override fun compareTo(other: ResolvedModuleIdentifier): Int =
        "$group:$name"
            .compareTo("${other.group}:${other.name}")
            .takeIf { it != 0 } ?: version.compareTo(other.version)
}

const val LOCAL_FILE_GROUP = "local-file"
