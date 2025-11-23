package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.DisplayArtifact
import kotlinx.serialization.StringFormat
import kotlinx.serialization.builtins.ListSerializer

class Visualizer(
    private val displayArtifacts: List<DisplayArtifact>,
) {
    fun visualizeArtifacts(format: StringFormat): String = format.encodeToString(ListSerializer(DisplayArtifact.serializer()), displayArtifacts)
}
