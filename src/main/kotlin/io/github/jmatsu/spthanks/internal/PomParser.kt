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

        val associatedUrl: String? = pomRoot["url"]?.text() ?: pomRoot["scm.url"]?.text()

        val displayNameCandidates = arrayOf(
                pomRoot["name"],
                pomRoot["description"],
                pomRoot["artifactId"]
        ).mapNotNull {
            it?.text()
        }

        val licenses: List<License> = pomRoot["licenses"]
                .childPaths()
                .map {
                    val name = it["name"]?.text()
                    val url = it["url"]?.text()
                    // Is distribution node required? :thinking_face:
                    License(
                            name = name,
                            url = url
                    )
                }

        val copyrightHolders = pomRoot["developers"]
                .childPaths()
                .mapNotNull {
                    it["name"]?.text()
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

    private fun GPathResult?.childPaths(): List<GPathResult> {
        return this?.children()?.map { it as GPathResult }.orEmpty()
    }
}