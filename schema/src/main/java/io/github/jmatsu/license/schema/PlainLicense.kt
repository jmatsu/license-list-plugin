package io.github.jmatsu.license.schema

interface PlainLicense : License {
    val key: LicenseKey
    val name: String
    val url: String?
}
