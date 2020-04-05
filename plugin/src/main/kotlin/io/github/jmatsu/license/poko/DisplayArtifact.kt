package io.github.jmatsu.license.poko

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class DisplayArtifact(
    @Transient
    override val key: String,
    override val displayName: String,
    override val url: String?,
    override val copyrightHolders: List<String>,
    override val licenses: List<PlainLicense>,
    @Transient
    override val keep: Boolean = true
) : io.github.jmatsu.license.schema.DisplayArtifact {

    @Required
    override val module: String
        get() = key
}
