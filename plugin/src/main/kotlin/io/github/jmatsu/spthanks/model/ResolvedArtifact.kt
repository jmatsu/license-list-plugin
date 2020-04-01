package io.github.jmatsu.spthanks.model

data class ResolvedArtifact(
    val id: ResolvedModuleIdentifier,
    val pomFile: ResolvedPomFile
)
