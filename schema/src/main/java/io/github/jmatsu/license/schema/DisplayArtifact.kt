package io.github.jmatsu.license.schema

interface DisplayArtifact : ArtifactDefinition<PlainLicense> {
    val module: String
}
