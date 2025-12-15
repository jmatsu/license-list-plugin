package io.github.jmatsu.license.internal

import io.github.jmatsu.license.model.LicenseSeed
import io.github.jmatsu.license.model.ResolvedPomFile
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PomParser(
    private val file: File,
) {
    fun parse(): ResolvedPomFile {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(file)
        val root = document.documentElement

        val associatedUrl: String? = root.getChildText("url") ?: root.getNestedChildText("scm", "url")

        val displayNameCandidates =
            listOfNotNull(
                root.getChildText("name"),
                root.getChildText("description"),
                root.getChildText("artifactId"),
            ).filter { it.isNotBlank() }

        val licenses: List<LicenseSeed> =
            root
                .getChildElement("licenses")
                ?.getChildElements("license")
                ?.map { licenseElement ->
                    val name = licenseElement.getChildText("name")
                    val url = licenseElement.getChildText("url")
                    LicenseSeed(
                        name = name,
                        url = url,
                    )
                }.orEmpty()

        val copyrightHolders =
            root
                .getChildElement("developers")
                ?.getChildElements("developer")
                ?.mapNotNull { developerElement ->
                    developerElement.getChildText("name")
                }.orEmpty()

        require(displayNameCandidates.isNotEmpty())

        return ResolvedPomFile(
            associatedUrl = associatedUrl,
            displayNameCandidates = displayNameCandidates,
            copyrightHolders = copyrightHolders,
            licenses = licenses,
        )
    }

    private fun Element.getChildText(tagName: String): String? {
        val nodeList = getElementsByTagName(tagName)
        if (nodeList.length == 0) return null
        // Get direct child only
        for (i in 0 until nodeList.length) {
            val node = nodeList.item(i)
            if (node.parentNode == this) {
                return node.textContent?.trim()?.takeIf { it.isNotBlank() }
            }
        }
        return null
    }

    private fun Element.getNestedChildText(
        parentTag: String,
        childTag: String,
    ): String? = getChildElement(parentTag)?.getChildText(childTag)

    private fun Element.getChildElement(tagName: String): Element? {
        val children = childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == tagName) {
                return child as Element
            }
        }
        return null
    }

    private fun Element.getChildElements(tagName: String): List<Element> {
        val result = mutableListOf<Element>()
        val children = childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == tagName) {
                result.add(child as Element)
            }
        }
        return result
    }
}
