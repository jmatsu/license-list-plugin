package io.github.jmatsu.spthanks.model

sealed class ResolveScope {
    abstract val name: String

    data class Variant(
        override val name: String
    ) : ResolveScope()

    data class Addition(
        override val name: String
    ) : ResolveScope()

    object Test : ResolveScope() {
        override val name: String = "test"
    }

    object AndroidTest : ResolveScope() {
        override val name: String = "androidTest"
    }

    /**
     * not yet supported
     */
    @Suppress("Unused")
    data class UserDefined(
        override val name: String
    ) : ResolveScope()

    /**
     * Only for internal uses
     */
    internal data class InternalUse(
        override val name: String
    ) : ResolveScope()

    object Unknown : ResolveScope() {
        override val name: String = "unknown"
    }
}
