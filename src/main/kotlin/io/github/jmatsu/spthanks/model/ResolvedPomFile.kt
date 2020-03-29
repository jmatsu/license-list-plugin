package io.github.jmatsu.spthanks.model

import io.github.jmatsu.spthanks.internal.PomParser

data class ResolvedPomFile(
        val associatedUrl: String?,
        val displayNameCandidates: List<String>,
        val licenses: List<PomParser.License>,
        val copyrightHolders: List<String>
) {
    val displayName: String
        get() = displayNameCandidates.first { it.isNotBlank() }
}