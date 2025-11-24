package io.github.jmatsu.license.model

sealed class ResolvedMetadata {
    abstract val associatedUrl: String?
    abstract val displayName: String
    abstract val licenses: List<LicenseSeed>
    abstract val copyrightHolders: List<String>
}

data class ResolvedPomFile(
    override val associatedUrl: String?,
    val displayNameCandidates: List<String>,
    override val licenses: List<LicenseSeed>,
    override val copyrightHolders: List<String>,
) : ResolvedMetadata() {
    override val displayName: String
        get() = displayNameCandidates.first { it.isNotBlank() }
}

data class ResolvedLocalFileMetadata(
    override val displayName: String,
    override val licenses: List<LicenseSeed>,
) : ResolvedMetadata() {
    override val associatedUrl: String? = null
    override val copyrightHolders: List<String> = emptyList()
}
