package io.github.jmatsu.spthanks.internal

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import io.github.jmatsu.spthanks.model.ResolvedPomFile
import java.io.File

class PomParser(
        private val file: File
) {
    data class License(
            val name: String?,
            val url: String?
    )

    fun parse(): ResolvedPomFile {
        val pomRoot = XmlSlurper(false, false).parse(file)

        val associatedUrl: String? = pomRoot["url"]?.trimText() ?: pomRoot["scm.url"]?.trimText()

        val displayNameCandidates = arrayOf(
                pomRoot["name"],
                pomRoot["description"],
                pomRoot["artifactId"]
        ).mapNotNull {
            it?.trimText()
        }

        val licenses: List<License> = pomRoot["licenses"]
                .childPaths()
                .map {
                    val name = it["name"]?.trimText()
                    val url = it["url"]?.trimText()
                    // Is distribution node required? :thinking_face:
                    License(
                            name = name,
                            url = url
                    )
                }

        val copyrightHolders = pomRoot["developers"]
                .childPaths()
                .mapNotNull {
                    it["name"]?.trimText()
                }

        require(displayNameCandidates.isNotEmpty())

        return ResolvedPomFile(
                associatedUrl = associatedUrl,
                displayNameCandidates = displayNameCandidates,
                copyrightHolders = copyrightHolders,
                licenses = licenses
        )
    }

    private operator fun GPathResult?.get(path: String): GPathResult? {
        return path.split(".").fold(this) { acc, name ->
            acc?.getProperty(name) as? GPathResult
        }
    }

    private fun GPathResult.trimText(): String? {
        return this.text()?.takeIf { it.isNotBlank() }
    }

    private fun GPathResult?.childPaths(): List<GPathResult> {
        return this?.children()?.map { it as GPathResult }.orEmpty()
    }
}