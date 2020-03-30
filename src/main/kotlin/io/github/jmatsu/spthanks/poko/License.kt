package io.github.jmatsu.spthanks.poko

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule

@Serializable
sealed class License {


    object Serialization {
        val module = SerializersModule {
            polymorphic(License::class) {
                LicenseKey::class with LicenseKey.serializer()
                PlainLicense::class with PlainLicense.serializer()
            }
        }
    }
}

@Serializable
data class LicenseKey(
        val value: String
) : License() {
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
        val name: String,
        val url: String
) : License()
