package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class ArtifactDefinition(
        val key: String,
        val displayName: String,
        val url: String?,
        val copyrightHolders: List<String>,
        val licenses: List<@ContextualSerialization License>
) : Comparable<ArtifactDefinition> {
    override fun compareTo(other: ArtifactDefinition): Int {
        return key.compareTo(other.key)
    }
}