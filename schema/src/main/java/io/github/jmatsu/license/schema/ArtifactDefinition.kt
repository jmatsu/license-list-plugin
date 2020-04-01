package io.github.jmatsu.license.schema

interface ArtifactDefinition {
    val key: String
    val displayName: String
    val url: String?
    val copyrightHolders: List<String>
    val licenses: List<LicenseKey>
}
