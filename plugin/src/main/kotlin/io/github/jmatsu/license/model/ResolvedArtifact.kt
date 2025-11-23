package io.github.jmatsu.license.model

data class ResolvedArtifact(
    val id: ResolvedModuleIdentifier,
    val metadata: ResolvedMetadata,
    val local: Boolean = false,
)
