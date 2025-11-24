package io.github.jmatsu.license.internal

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import io.github.jmatsu.license.model.LicenseSeed
import io.github.jmatsu.license.model.ResolvedPomFile
import java.io.File

class PomParser(
    private val file: File,
) {
    fun parse(): ResolvedPomFile {
        val pomRoot = XmlSlurper(false, false).parse(file)

        val associatedUrl: String? = pomRoot["url"]?.trimText() ?: pomRoot["scm.url"]?.trimText()

        val displayNameCandidates =
            arrayOf(
                pomRoot["name"],
                pomRoot["description"],
                pomRoot["artifactId"],
            ).mapNotNull {
                it?.trimText()
            }

        val licenses: List<LicenseSeed> =
            pomRoot["licenses"]
                .childPaths()
                .map {
                    val name = it["name"]?.trimText()
                    val url = it["url"]?.trimText()
                    // Is distribution node required? :thinking_face:
                    LicenseSeed(
                        name = name,
                        url = url,
                    )
                }

        val copyrightHolders =
            pomRoot["developers"]
                .childPaths()
                .mapNotNull {
                    it["name"]?.trimText()
                }

        require(displayNameCandidates.isNotEmpty())

        return ResolvedPomFile(
            associatedUrl = associatedUrl,
            displayNameCandidates = displayNameCandidates,
            copyrightHolders = copyrightHolders,
            licenses = licenses,
        )
    }

    private operator fun GPathResult?.get(path: String): GPathResult? =
        path.split(".").fold(this) { acc, name ->
            acc?.getProperty(name) as? GPathResult
        }

    private fun GPathResult.trimText(): String? = this.text()?.takeIf { it.isNotBlank() }

    private fun GPathResult?.childPaths(): List<GPathResult> = this?.children()?.map { it as GPathResult }.orEmpty()
}
