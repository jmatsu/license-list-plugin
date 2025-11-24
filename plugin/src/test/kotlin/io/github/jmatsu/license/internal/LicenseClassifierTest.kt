package io.github.jmatsu.license.internal

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.expect

class LicenseClassifierTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "The GNU Affero General Public License, Version 3.0",
            "The GNU Affero General Public License 3.0",
            "The GNU Affero General Public License 3",
            "agpl 3",
            "agpl3",
            "http://example.com/agpl-3.0",
            "http://example.com/agpl-3.0/salt",
        ],
    )
    fun `agpl30`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.AGPL30) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "The Apache Software License, Version 2.0",
            "The Apache Software License 2.0",
            "Apache License 2.0",
            "Apache 2.0",
            "http://www.apache.org/licenses/LICENSE-2.0.txt",
            "http://example.com/Apache-2.0",
            "http://example.com/Apache-2.0/salt",
        ],
    )
    fun `apache2`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.Apache20) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "BSD 2-Clause \"Simplified\" License",
            "http://example.com/BSD-2-Clause",
            "http://example.com/BSD-2-Clause/salt",
            "License BSD-Clause/simplified",
        ],
    )
    fun `bsd2`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.BSD2C) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "BSD 3-Clause \"New\" or \"Revised\" License",
            "http://example.com/BSD-3-Clause",
            "http://example.com/BSD-3-Clause/salt",
            "License BSD-Clause/new",
            "License BSD-Clause/revised",
        ],
    )
    fun `bsd3`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.BSD3C) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "BSD 4-Clause \"Original\" or \"Old\" License",
            "http://example.com/BSD-4-Clause",
            "http://example.com/BSD-4-Clause/salt",
            "License BSD-Clause/original",
            "License BSD-Clause/old",
        ],
    )
    fun `bsd4`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.BSD4C) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Eclipse Public License 1.0",
            "Eclipse Public License 1",
            "epl1.0",
            "epl 1.0",
            "epl1",
            "epl 1",
        ],
    )
    fun `epl1`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.EPL10) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Eclipse Public License 2.0",
            "Eclipse Public License 2",
            "epl2.0",
            "epl 2.0",
            "epl2",
            "epl 2",
        ],
    )
    fun `epl2`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.EPL20) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Mozilla Public License 2.0",
            "Mozilla Public License 2",
            "mpl2.0",
            "mpl 2.0",
            "mpl2",
            "mpl 2",
        ],
    )
    fun `mpl2`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.MPL20) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "GNU Lesser General Public License v2.1",
            "GNU Lesser General Public License 2.1",
            "lgpl2.1",
            "lgpl 2.1",
        ],
    )
    fun `lgpl2_1`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.LGPL21) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "GNU Lesser General Public License v3.0",
            "GNU Lesser General Public License v3",
            "GNU Lesser General Public License 3.0",
            "GNU Lesser General Public License 3",
            "lgpl3.0",
            "lgpl 3.0",
            "lgpl3",
            "lgpl 3",
        ],
    )
    fun `lgpl3`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.LGPL30) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "GNU General Public License v2.0",
            "GNU General Public License 2.0",
            "GNU General Public License v2",
            "GNU General Public License 2",
            "gpl2.0",
            "gpl 2.0",
            "gpl2",
            "gpl 2",
        ],
    )
    fun `gpl2`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.GPL20) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "GNU General Public License v3.0",
            "GNU General Public License 3.0",
            "GNU General Public License v3",
            "GNU General Public License 3",
            "gpl3.0",
            "gpl 3.0",
            "gpl3",
            "gpl 3",
        ],
    )
    fun `gpl3`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.GPL30) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "MIT License",
            "mit",
        ],
    )
    fun `mit`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.MIT) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "the Facebook Platform License",
            "facebook",
        ],
    )
    fun `facebook`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.FacebookSDK) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Android Software Development Kit License",
            "android",
        ],
    )
    fun `android sdk`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.AndroidSDK) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "MoPub SDK License Agreement",
            "mopub",
        ],
    )
    fun `mopub`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.MoPubSDK) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Creative Commons Attribution 4.0 International Public License",
            "cc4.0",
            "cc 4.0",
            "cc 4",
            "cc4",
        ],
    )
    fun `cc4`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.CC40) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Bouncy Castle Licence",
            "bcl",
        ],
    )
    fun `bcl`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.Bcl) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "",
        ],
    )
    fun `unlicense`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.Unlicense) {
            licenseClassifier.guess()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "The GNU Affero General Public License",
            "agpl",
            "agpl 5",
            "The Apache Software License",
            "apache",
            "apache 5",
            "BSD Clause License",
            "BSD",
            "BSD 7",
            "Eclipse Public License",
            "epl",
            "epl 8",
        ],
    )
    fun `undetermined`(name: String) {
        val licenseClassifier = LicenseClassifier(name)

        expect(LicenseClassifier.GuessedLicense.Undetermined(name = name, url = "")) {
            licenseClassifier.guess()
        }
    }
}
