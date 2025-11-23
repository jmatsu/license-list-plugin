package io.github.jmatsu.license.poko

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(Scope.Companion::class)
data class Scope(
    override val name: String
) : io.github.jmatsu.license.schema.Scope {

    // TODO Make Scope inline class if Serialization supports it, then I can remove this
    companion object : KSerializer<Scope> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Scope", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Scope {
            return Scope(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: Scope) {
            encoder.encodeString(value.name)
        }

        val StubScope: Scope = Scope("stub")
    }
}
