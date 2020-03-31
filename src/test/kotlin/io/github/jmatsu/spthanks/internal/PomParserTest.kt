package io.github.jmatsu.spthanks.internal

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import kotlin.test.expect

class PomParserTest {
    companion object {
        @JvmStatic
        fun providePomFiles(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(
                            "pom-1.xml",
                            "Example1",
                            listOf("Example1", "", "example"),
                            "https://github.com/jmatsu",
                            listOf("jmatsu"),
                            listOf(
                                    PomParser.License(
                                            name = "The Apache Software License, Version 2.0",
                                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                                    )
                            )
                    )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("providePomFiles")
    fun `parse`(
            filepath: String,
            displayName: String,
            displayNameCandidates: List<String>,
            associatedUrl: String,
            copyrightHolders: List<String>,
            licenses: List<PomParser.License>
    ) {
        var pomFile: File? = null
        try {
            pomFile = File.createTempFile("pom-parser-test", ".xml")
            pomFile.writeText(javaClass.classLoader.getResourceAsStream(filepath).bufferedReader().readText())
            val parseResult = PomParser(pomFile).parse()

            expect(displayName) {
                parseResult.displayName
            }

            expect(displayNameCandidates) {
                parseResult.displayNameCandidates
            }

            expect(associatedUrl) {
                parseResult.associatedUrl
            }

            expect(copyrightHolders) {
                parseResult.copyrightHolders
            }

            expect(licenses) {
                parseResult.licenses
            }
        } finally {
            pomFile?.delete()
        }
    }
}