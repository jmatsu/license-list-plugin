package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.*

sealed class License

@Serializable
data class LicenseKey(
    val value: String
) : License() {
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
    val key: String,
    val name: String,
    val url: String
) : License()
