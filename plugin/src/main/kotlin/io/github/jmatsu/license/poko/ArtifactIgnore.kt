@file:UseContextualSerialization

package io.github.jmatsu.license.poko

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(ArtifactIgnore.Companion::class)
data class ArtifactIgnore(
    override val regex: Regex,
) : io.github.jmatsu.license.schema.ArtifactIgnore {
    companion object : KSerializer<ArtifactIgnore> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("ArtifactIgnore", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ArtifactIgnore = ArtifactIgnore(regex = Regex(decoder.decodeString()))

        override fun serialize(
            encoder: Encoder,
            value: ArtifactIgnore,
        ) {
            encoder.encodeString(value.regex.pattern)
        }
    }
}
