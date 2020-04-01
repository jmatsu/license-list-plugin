package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

@Serializable
data class Scope(
    val name: String
) {

    // TODO Make Scope inline class if Serialization supports it, then I can remove this
    @Serializer(forClass = Scope::class)
    companion object : KSerializer<Scope> {
        override val descriptor: SerialDescriptor =
            SerialDescriptor("Scope", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Scope {
            return Scope(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: Scope) {
            encoder.encodeString(value.name)
        }
    }
}
