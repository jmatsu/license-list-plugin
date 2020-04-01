package io.github.jmatsu.license.internal

import java.util.Locale

class LicenseClassifier(
    val name: String?
) {
    // https://choosealicense.com/
    object PredefinedKey {
        const val AGPL_3_0 = "agpl-3.0"
        const val APACHE_2_0 = "apache-2.0"
        const val BSD_2_CLAUSE = "bsd-2-clause"
        const val BSD_3_CLAUSE = "bsd-3-clause"
        const val CC0_1_0 = "cc0-1.0"
        const val EPL_2_0 = "epl-2.0"
        const val GPL_2_0 = "gpl-2.0"
        const val GPL_3_0 = "gpl-3.0"
        const val LGPL_2_1 = "lgpl-2.1"
        const val LGPL_3_0 = "lgpl-3.0"
        const val MIT = "mit"
        const val MPL_2_0 = "mpl-2.0"
        const val UNLICENSE = "unlicense"

        // The above are found in GitHub License API and self-hosted in this repository
        // The below are fetched from https://help.github.com/en/github/creating-cloning-and-archiving-repositories/licensing-a-repository
        const val AFL_3_0 = "afl-3.0"
        const val ARTISTIC_2_0 = "artistic-2.0"
        const val BSL_1_0 = "bsl-1.0"
        const val BSD_3_CLAUSE_CLEAR = "bsd-3-clause-clear"
        const val BSD_4_CLAUSE = "bsd-4-clause"
        const val CC_4_0 = "cc-by-4.0"
        const val CC_SA_4_0 = "cc-by-sa-4.0"
        const val WTFPL = "wtfpl"
        const val ECL_2_0 = "ecl-2.0"
        const val EPL_1_0 = "epl-1.0"
        const val EUPL_1_1 = "eupl-1.1"
        const val ISC = "isc"
        const val LPPL_1_3C = "lppl-1.3c"
        const val MS_PL = "ms-pl"
        const val OSL_3_0 = "osl-3.0"
        const val POSTGRESQL = "postgresql"
        const val OFL_1_1 = "ofl-1.1"
        const val NCSA = "ncsa"
        const val ZLIB = "zlib"

        const val MOPUB_SDK = "mopub-sdk"
        const val ANDROID_SDK = "android-sdk"
        const val FACEBOOK_SDK = "facebook-sdk"
        const val BCL = "bcl"

        const val UNDETERMINED = "undetermined"
    }

    sealed class GuessedLicense {
        abstract val key: String
        abstract val name: String
        abstract val url: String

        abstract class GitHubAPICompatibleLicense : GuessedLicense() {
            override val url: String
                get() = "https://api.github.com/licenses/$key"
        }

        object AGPL30 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.AGPL_3_0
            override val name: String = "GNU Affero General Public License v3.0"
        }

        object Apache20 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.APACHE_2_0
            override val name: String = "Apache License 2.0"
        }

        object BSD2C : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.BSD_2_CLAUSE
            override val name: String = "BSD 2-Clause \"Simplified\" License"
        }

        object BSD3C : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.BSD_3_CLAUSE
            override val name: String = "BSD 3-Clause \"New\" or \"Revised\" License"
        }

        object CC010 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.CC0_1_0
            override val name: String = "Creative Commons Zero v1.0 Universal"
        }

        object EPL20 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.EPL_2_0
            override val name: String = "Eclipse Public License 2.0"
        }

        object GPL20 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.GPL_2_0
            override val name: String = "GNU General Public License v2.0"
        }

        object GPL30 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.GPL_3_0
            override val name: String = "GNU General Public License v3.0"
        }

        object LGPL21 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.LGPL_2_1
            override val name: String = "GNU Lesser General Public License v2.1"
        }

        object LGPL30 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.LGPL_3_0
            override val name: String = "GNU Lesser General Public License v3.0"
        }

        object MIT : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.MIT
            override val name: String = "MIT License"
        }

        object MPL20 : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.MPL_2_0
            override val name: String = "Mozilla Public License 2.0"
        }

        object Unlicense : GitHubAPICompatibleLicense() {
            override val key: String = PredefinedKey.UNLICENSE
            override val name: String = "The Unlicense"
        }

        // ---

        object BSD4C : GuessedLicense() {
            override val key: String = PredefinedKey.BSD_4_CLAUSE
            override val name: String = "BSD 4-Clause \"Original\" or \"Old\" License"
            override val url: String = "https://spdx.org/licenses/BSD-4-Clause.html"
        }

        object EPL10 : GuessedLicense() {
            override val key: String = PredefinedKey.EPL_1_0
            override val name: String = "Eclipse Public License 1.0"
            override val url: String = "https://opensource.org/licenses/EPL-1.0"
        }

        object CC40 : GuessedLicense() {
            override val key: String = PredefinedKey.CC_4_0
            override val name: String = "Creative Commons Attribution 4.0 International Public License"
            override val url: String = "https://creativecommons.org/licenses/by/4.0/legalcode"
        }

        // ---

        object MoPubSDK : GuessedLicense() {
            override val key: String = PredefinedKey.MOPUB_SDK
            override val name: String = "MoPub SDK License Agreement"
            override val url: String = "https://www.mopub.com/legal/sdk-license-agreement/"
        }

        object AndroidSDK : GuessedLicense() {
            override val key: String = PredefinedKey.ANDROID_SDK
            override val name: String = "Android Software Development Kit License"
            override val url: String = "https://developer.android.com/studio/terms.html"
        }

        object FacebookSDK : GuessedLicense() {
            override val key: String = PredefinedKey.FACEBOOK_SDK
            override val name: String = "the Facebook Platform License"
            override val url: String = "https://github.com/facebook/facebook-android-sdk/blob/master/LICENSE.txt"
        }

        object Bcl : GuessedLicense() {
            override val key: String = PredefinedKey.BCL
            override val name: String = "Bouncy Castle Licence"
            override val url: String = "http://www.bouncycastle.org/licence.html"
        }

        // ---

        data class Undetermined(
            override val key: String = PredefinedKey.UNDETERMINED,
            override val name: String,
            override val url: String
        ) : GuessedLicense()
    }

    companion object {
        val predefinedGuessedLicenses: List<GuessedLicense> by lazy {
            GuessedLicense::class.sealedSubclasses.filter { it.isFinal }.map { it.objectInstance }.filterIsInstance(GuessedLicense::class.java)
        }

        val versionRegexp = Regex("(\\d\\.\\d|\\d)")
        val isApache = wordMatch("apache")
        val isBSD = wordMatch("bsd[\\d]?")
        val isBSD2 = wordMatch("simplified")
        val isBSD3 = wordMatch("(new|revised)")
        val isBSD4 = wordMatch("(original|old)")
        val isEPL = wordMatch("eclipse\\b.*public")
        val isEPL_2nd = wordMatch("epl[\\d]?")
        val isMPL = wordMatch("mozilla\\b.*public")
        val isMPL_2nd = wordMatch("mpl[\\d]?")
        val isLGPL = wordMatch("lesser\\b.*general\\b.*public")
        val isLGPL_2nd = wordMatch("lgpl[\\d]?")
        val isAGPL = wordMatch("affero\\b.*general\\b.*public")
        val isAGPL_2nd = wordMatch("agpl[\\d]?")
        val isGPL = wordMatch("general\\b.*public")
        val isGPL_2nd = wordMatch("gpl[\\d]?")
        val isCC = wordMatch("creative\\b.*commons")
        val isCC_2nd = wordMatch("cc[\\d]?")
        val isMIT = wordMatch("mit")
        val isFacebookSDK = wordMatch("facebook")
        val isMoPubSDK = wordMatch("mopub")
        val isAndroidSDK = wordMatch("android")

        private fun wordMatch(word: String): Regex {
            return "\\b$word\\b".toRegex()
        }
    }

    fun guess(): GuessedLicense {
        if (name == null) {
            return GuessedLicense.Unlicense
        }

        fun String.normalize(): String = toLowerCase(Locale.US).replace("[,\"]|licen[sc]e".toRegex(), " ").trim()
        operator fun Regex.contains(text: String): Boolean = containsMatchIn(text)

        val fallbackLicense = GuessedLicense.Undetermined(
            name = name,
            url = ""
        )
        val version = versionRegexp.find(name)?.groupValues?.drop(1)?.firstOrNull()?.takeIf { it.isNotBlank() }

        return when (val text = name.normalize()) {
            in isAGPL, in isAGPL_2nd -> {
                when (version) {
                    "3", "3.0" -> GuessedLicense.AGPL30
                    else -> fallbackLicense
                }
            }
            in isApache -> {
                when (version) {
                    "2", "2.0" -> GuessedLicense.Apache20
                    else -> fallbackLicense
                }
            }
            in isBSD -> {
                if (version == "2" || text in isBSD2) {
                    GuessedLicense.BSD2C
                } else if (version == "3" || text in isBSD3) {
                    GuessedLicense.BSD3C
                } else if (version == "4" || text in isBSD4) {
                    GuessedLicense.BSD4C
                } else {
                    fallbackLicense
                }
            }
            in isEPL, in isEPL_2nd -> {
                when (version) {
                    "1", "1.0" -> GuessedLicense.EPL10
                    "2", "2.0" -> GuessedLicense.EPL20
                    else -> fallbackLicense
                }
            }
            in isMPL, in isMPL_2nd -> {
                when (version) {
                    "2", "2.0" -> GuessedLicense.MPL20
                    else -> fallbackLicense
                }
            }
            in isLGPL, in isLGPL_2nd -> {
                when (version) {
                    "2.1" -> GuessedLicense.LGPL21
                    "3", "3.0" -> GuessedLicense.LGPL30
                    else -> fallbackLicense
                }
            }
            in isGPL, in isGPL_2nd -> {
                when (version) {
                    "2", "2.0" -> GuessedLicense.GPL20
                    "3", "3.0" -> GuessedLicense.GPL30
                    else -> fallbackLicense
                }
            }
            in isMIT -> GuessedLicense.MIT
            in isFacebookSDK -> GuessedLicense.FacebookSDK
            in isMoPubSDK -> GuessedLicense.MoPubSDK
            in isAndroidSDK -> GuessedLicense.AndroidSDK
            in isCC, in isCC_2nd -> {
                when (version) {
                    "4", "4.0" -> GuessedLicense.CC40
                    else -> GuessedLicense.CC010
                }
            }
            "" -> GuessedLicense.Unlicense
            else -> fallbackLicense
        }
    }
}
