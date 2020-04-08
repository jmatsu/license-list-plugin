package io.github.jmatsu.license.example.poko

import com.squareup.moshi.JsonClass
import io.github.jmatsu.license.schema.ArtifactDefinition

@JsonClass(generateAdapter = true)
class ArtifactDifinitionPoko(
    override val copyrightHolders: List<String>?,
    override val displayName: String,
    override val key: String,
    override val licenses: List<PlainLicensePoko>,
    override val url: String?,
    override val keep: Boolean = true
) : ArtifactDefinition<PlainLicensePoko>
