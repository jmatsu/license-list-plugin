package io.github.jmatsu.license.example.poko

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import io.github.jmatsu.license.schema.LicenseKey
import io.github.jmatsu.license.schema.PlainLicense

class LicenseKeyPoko(override val value: String) : LicenseKey {
    object Adapter {
        @ToJson
        fun toJson(poko: LicenseKeyPoko): String {
            return poko.value
        }

        @FromJson
        fun fromJson(json: String): LicenseKeyPoko {
            return LicenseKeyPoko(json)
        }
    }
}

@JsonClass(generateAdapter = true)
class PlainLicensePoko(override val key: LicenseKeyPoko, override val name: String, override val url: String? = null) : PlainLicense
