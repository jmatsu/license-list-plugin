package io.github.jmatsu.license.model

data class ResolvedArtifact(
    val id: ResolvedModuleIdentifier,
    val pomFile: ResolvedPomFile
)
