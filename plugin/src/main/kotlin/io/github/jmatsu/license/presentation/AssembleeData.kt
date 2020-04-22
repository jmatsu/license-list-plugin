package io.github.jmatsu.license.presentation

import io.github.jmatsu.license.poko.ArtifactDefinition
import io.github.jmatsu.license.poko.PlainLicense
import io.github.jmatsu.license.poko.Scope

data class AssembleeData(
    val scopedArtifacts: Map<Scope, List<ArtifactDefinition>>,
    val licenses: List<PlainLicense>
)
