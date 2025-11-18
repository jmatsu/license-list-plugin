package io.github.jmatsu.license.migration

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(LibraryInfo.Companion::class)
data class LibraryInfo(
    val artifact: String,
    val name: String = "",
    val filename: String = "",
    val copyrightHolder: String = "",
    val notice: String = "",
    val license: String = "",
    val licenseUrl: String = "",
    val url: String = "",
    val skip: Boolean = false,
    val forceGenerate: Boolean = false,
    val authors: List<String> = emptyList(),
    val copyrightHolders: List<String> = emptyList(),
    val author: String = ""
) {
    companion object : KSerializer<LibraryInfo> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LibraryInfo") {
            element("artifact", String.serializer().descriptor)
            element("name", String.serializer().descriptor, isOptional = true)
            element("filename", String.serializer().descriptor, isOptional = true)
            element("copyrightHolder", String.serializer().descriptor, isOptional = true)
            element("notice", String.serializer().descriptor, isOptional = true)
            element("license", String.serializer().descriptor, isOptional = true)
            element("licenseUrl", String.serializer().descriptor, isOptional = true)
            element("url", String.serializer().descriptor, isOptional = true)
            element("skip", Boolean.serializer().descriptor, isOptional = true)
            element("forceGenerate", Boolean.serializer().descriptor, isOptional = true)
            element("authors", ListSerializer(String.serializer()).descriptor, isOptional = true)
            element("copyrightHolders", ListSerializer(String.serializer()).descriptor, isOptional = true)
            element("author", String.serializer().descriptor, isOptional = true)
        }

        override fun deserialize(decoder: Decoder): LibraryInfo {
            var artifact: String? = null
            var name: String = ""
            var filename: String = ""
            var copyrightHolder: String = ""
            var notice: String = ""
            var license: String = ""
            var licenseUrl: String = ""
            var url: String = ""
            var skip: Boolean = false
            var forceGenerate: Boolean = false
            var authors: List<String> = emptyList()
            var copyrightHolders: List<String> = emptyList()
            var author: String = ""

            decoder.beginStructure(descriptor).apply {
                loop@ while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        CompositeDecoder.DECODE_DONE -> break@loop
                        0 -> artifact = decodeStringElement(descriptor, index)
                        1 -> name = decodeStringElement(descriptor, index)
                        2 -> filename = decodeStringElement(descriptor, index)
                        3 -> copyrightHolder = decodeStringElement(descriptor, index)
                        4 -> notice = decodeStringElement(descriptor, index)
                        5 -> license = decodeStringElement(descriptor, index)
                        6 -> licenseUrl = decodeStringElement(descriptor, index)
                        7 -> url = decodeStringElement(descriptor, index)
                        8 -> skip = decodeBooleanElement(descriptor, index)
                        9 -> forceGenerate = decodeBooleanElement(descriptor, index)
                        10 -> authors = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                        11 -> copyrightHolders = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                        12 -> author = decodeStringElement(descriptor, index)
                        else -> throw SerializationException("Unknown index $index")
                    }
                }
            }.endStructure(descriptor)

            return LibraryInfo(
                artifact = artifact!!,
                name = name,
                filename = filename,
                copyrightHolder = copyrightHolder,
                notice = notice,
                license = license,
                licenseUrl = licenseUrl,
                url = url,
                skip = skip,
                forceGenerate = forceGenerate,
                authors = authors,
                copyrightHolders = copyrightHolders,
                author = author
            )
        }

        override fun serialize(encoder: Encoder, value: LibraryInfo) {
            TODO("not supported")
        }
    }
}
