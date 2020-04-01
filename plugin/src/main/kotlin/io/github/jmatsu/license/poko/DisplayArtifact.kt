package io.github.jmatsu.license.poko

data class DisplayArtifact(
    override val key: String,
    override val displayName: String,
    override val url: String?,
    override val copyrightHolders: List<String>,
    override val licenses: List<PlainLicense>
) : io.github.jmatsu.license.schema.ArtifactDefinition<PlainLicense>
