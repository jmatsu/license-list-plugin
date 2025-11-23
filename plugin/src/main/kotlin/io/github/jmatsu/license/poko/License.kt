package io.github.jmatsu.license.poko

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

sealed class License

@Serializable(LicenseKey.Companion::class)
data class LicenseKey(
    override val value: String,
) : License(),
    io.github.jmatsu.license.schema.LicenseKey {
    // TODO Make LicenseKey inline class if Serialization supports it, then I can remove this
    companion object : KSerializer<LicenseKey> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("LicenseKey", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): LicenseKey = LicenseKey(decoder.decodeString())

        override fun serialize(
            encoder: Encoder,
            value: LicenseKey,
        ) {
            encoder.encodeString(value.value)
        }
    }
}

@Serializable
data class PlainLicense(
    @Required
    override val key: LicenseKey,
    override val name: String,
    override val url: String? = null,
) : License(),
    io.github.jmatsu.license.schema.PlainLicense
