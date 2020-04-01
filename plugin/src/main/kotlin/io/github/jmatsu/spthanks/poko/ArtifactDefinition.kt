package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.Serializable

@Serializable
data class ArtifactDefinition(
    val key: String,
    val displayName: String,
    val url: String?,
    val copyrightHolders: List<String>,
    val licenses: List<LicenseKey>
) : Comparable<ArtifactDefinition> {

    override fun compareTo(other: ArtifactDefinition): Int {
        // : is a separator and use secondary order
        val own = key.split(":")
        val others = other.key.split(":")

        require(own.size == others.size)

        own.indices.forEach {
            val value = own[it].compareTo(others[it])

            if (value != 0) {
                return value
            }
        }

        return 0
    }
}
