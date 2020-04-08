package io.github.jmatsu.license.schema

interface ArtifactDefinition<T : License> {
    val key: String
    val displayName: String
    val url: String?
    val copyrightHolders: List<String>?
    val licenses: List<T>
    val keep: Boolean
}
