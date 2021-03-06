package io.github.jmatsu.license.poko

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.Required
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

sealed class License

@Serializable
data class LicenseKey(
    override val value: String
) : License(), io.github.jmatsu.license.schema.LicenseKey {
    // TODO Make LicenseKey inline class if Serialization supports it, then I can remove this
    @Serializer(forClass = LicenseKey::class)
    companion object : KSerializer<LicenseKey> {
        override val descriptor: SerialDescriptor =
            SerialDescriptor("LicenseKey", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): LicenseKey {
            return LicenseKey(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: LicenseKey) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable
data class PlainLicense(

    @Required
    override val key: LicenseKey,
    override val name: String,
    override val url: String? = null
) : License(), io.github.jmatsu.license.schema.PlainLicense
