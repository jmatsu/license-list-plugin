package io.github.jmatsu.license.poko

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.Transient

private typealias PokoArtifactDefinition = io.github.jmatsu.license.schema.ArtifactDefinition<LicenseKey>

@Serializable
data class ArtifactDefinition(
    override val key: String,
    override val displayName: String,
    override val url: String?,
    override val copyrightHolders: List<String>,
    override val licenses: List<LicenseKey>,
    override val keep: Boolean = false
) : Comparable<ArtifactDefinition>, PokoArtifactDefinition {

    override fun compareTo(other: ArtifactDefinition): Int {
        // : is a separator and use secondary order
        val own = key.split(":")
        val others = other.key.split(":")

        require(own.size == others.size)

        own.indices.forEach {
            val value = own[it].compareTo(others[it])

            if (value != 0) {
                return value
            }
        }

        return 0
    }

    /**
     * The serializer that deserializes keep properly and serializes *keep* attribute iff it's true
     */
    @Serializer(forClass = ArtifactDefinition::class)
    companion object : KSerializer<ArtifactDefinition> {
        private val coreSerializer: KSerializer<OptionalKeep>
            get() = OptionalKeep.serializer()

        override val descriptor: SerialDescriptor by lazy {
            // Rename
            object : SerialDescriptor by coreSerializer.descriptor {
                override val serialName: String = "ArtifactDefinition"
            }
        }

        override fun deserialize(decoder: Decoder): ArtifactDefinition {
            val optionalKeep = coreSerializer.deserialize(decoder)

            return ArtifactDefinition(
                key = optionalKeep.key,
                displayName = optionalKeep.displayName,
                url = optionalKeep.url,
                copyrightHolders = optionalKeep.copyrightHolders,
                licenses = optionalKeep.licenses,
                keep = optionalKeep.keep
            )
        }

        override fun serialize(encoder: Encoder, value: ArtifactDefinition) {
            return if (value.keep) {
                Kept.serializer().serialize(
                    encoder = encoder,
                    value = Kept(
                        key = value.key,
                        displayName = value.displayName,
                        url = value.url,
                        copyrightHolders = value.copyrightHolders,
                        licenses = value.licenses,
                        keep = true
                    )
                )
            } else {
                WithoutKeep.serializer().serialize(
                    encoder = encoder,
                    value = WithoutKeep(
                        key = value.key,
                        displayName = value.displayName,
                        url = value.url,
                        copyrightHolders = value.copyrightHolders,
                        licenses = value.licenses,
                        keep = false
                    )
                )
            }
        }
    }
}

@Serializable
private class OptionalKeep(
    @Required
    override val key: String,
    override val displayName: String,
    override val url: String?,
    override val copyrightHolders: List<String>,
    override val licenses: List<LicenseKey>,
    override val keep: Boolean = false
) : PokoArtifactDefinition

@Serializable
private class WithoutKeep(
    @Required
    override val key: String,
    override val displayName: String,
    override val url: String?,
    override val copyrightHolders: List<String>,
    override val licenses: List<LicenseKey>,

    @Transient
    override val keep: Boolean = false
) : PokoArtifactDefinition

@Serializable
private class Kept(
    @Required
    override val key: String,
    override val displayName: String,
    override val url: String?,
    override val copyrightHolders: List<String>,
    override val licenses: List<LicenseKey>,
    override val keep: Boolean = false
) : PokoArtifactDefinition
