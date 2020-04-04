package io.github.jmatsu.license.poko

import io.github.jmatsu.license.schema.ArtifactIgnore
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class ArtifactIgnore(
    // To suppress a serializer not found error
    @ContextualSerialization override val regex: Regex
) : ArtifactIgnore {
    @Serializer(forClass = ArtifactIgnore::class)
    companion object : KSerializer<ArtifactIgnore> {
        override val descriptor: SerialDescriptor =
            SerialDescriptor("ArtifactIgnore", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ArtifactIgnore {
            return ArtifactIgnore(regex = Regex(decoder.decodeString()))
        }

        override fun serialize(encoder: Encoder, value: ArtifactIgnore) {
            encoder.encodeString(value.regex.pattern)
        }
    }
}
