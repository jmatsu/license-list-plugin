package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.DisplayArtifact
import kotlinx.serialization.builtins.list

class Visualizer(
    private val displayArtifacts: List<DisplayArtifact>
) {
    sealed class Style {
        object JsonStyle : Style()
        object HtmlStyle : Style()
    }


    fun visualizeArtifacts(style: Style): String {
        return when (style) {
            Style.JsonStyle -> {
                Convention.DisplayJson.stringify(DisplayArtifact.serializer().list, displayArtifacts)
            }
            Style.HtmlStyle -> {

                TODO()
            }
        }
    }
}
