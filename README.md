# License List Plugin

[![jmatsu](https://circleci.com/gh/jmatsu/license-list-plugin.svg?style=svg)](https://circleci.com/gh/jmatsu/license-list-plugin) ![master](https://github.com/jmatsu/license-list-plugin/workflows/Run%20build%20and%20test/badge.svg?branch=master)

Plugin : [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Gradle%20Plugin&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fio%2Fgithub%2Fjmatsu%2Flicense-list-gradle%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/io.github.jmatsu.license-list)
Schema lib : [ ![Download](https://api.bintray.com/packages/jmatsu/maven/license-list-schema/images/download.svg?version=latest) ](https://bintray.com/jmatsu/maven/license-list-schema/latest/link)

License List Plugin is a Gradle plugin to manage artifacts' licenses that your Android project uses. It can generate the data source as human readable or handy format.

*This plugin is still under development. Breaking changes may be introduced until 1.0.0.*

For those who have been using `0.3` or lower, version `0.4` has breaking changes. Please read [0.4 breaking changes](#04-breaking-changes)

## Guide

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
    1. [Installation](#installation)
    2. [About management files and syntax](#about-management-files-and-syntax)
        1. [artifact-definition.yml](#artifact-definitionyml)
        2. [license-catalog.yml](#license-catalogyml)
        3. [.artifactignore](#artifactignore)
    2. [Manage files](#manage-files)
        1. [The basic management cycle](#the-basic-management-cycle)
        2. [Generate a license viewer or its resource](#genetera-a-license-viewer-or-its-resource)
3. [Tasks](#tasks)
    1. [Initialize](#initialize)
    2. [Inspect](#inspect)
    3. [Validate](#validate)
    4. [Merge/Update](#mergeupdate)
    5. [Visualize](#visualize)
4. [Extension](#extension)
5. [Tips](#tips)
    1. [license-tools-plugin migration](#for-license-tools-plugin-users)
    2. [Exclude specific groups/artifacts](#exclude-specific-groupsartifacts)
    3. [Add other configurations like WearApp](#additional-configurations-like-wearapp)
    4. [Custom variant-aware configurations](#custom-variant-aware-configurations)
    5. [Html template customization](#html-customization)
    6. [Render Json output](#render-json)
6. [Known limitation](#limitations)
7. [Migration](#migration)
    1. [since 0.4](#04-breaking-changes)
8. [LICENSE](#license)

## Introduction

The goals of this plugin are the following

- Easy to add/delete/change licenses through human readable text. (either of Yaml, Json)
- Flexible visualization of licenses. (Html template injection, Json export)
- Whitelabel support using productFlavors or buildTypes for Android project. (Possible to manage for each variants)

Yaml configuration example is like the following.

```
release:
    androidx.activity:
      - key: activity
        displayName: Activity
        url: https://developer.android.com/jetpack/androidx
        copyrightHolders:
          - The Android Open Source Project
        licenses:
          - apache-2.0
```

This plugin can generate HTML or a json file for the license viewer based on the management file.

Sample view using json | The default html layout
:---|:---
<img src="./assets/recyclerViewSample.png" width="300"> | <img src="./assets/webViewSample.png" width="300">

## Getting Started

### Installation

#### Configure your project

Apply the plugin to "com.android.application" modules.

**For example, plugins block in Kotlin**

ref: https://plugins.gradle.org/plugin/io.github.jmatsu.license-list#kotlin-usage

```kotlin
plugins {
  id("com.android.application")
  id("io.github.jmatsu.license-list") version ("<version}>")
}
```

<details>

**Groovy**

ref: https://plugins.gradle.org/plugin/io.github.jmatsu.license-list#groovy-usage

```groovy
// Legacy groovy example.

buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "io.github.jmatsu:license-list-gradle:<version>"
  }
}

apply plugin: "com.android.application"
apply plugin: "io.github.jmatsu.license-list"
```

</details>

#### Start managing artifacts and/or licenses

You can generate management files based on the current dependencies.

If you need to manage only one variant, then it's better to configure this plugin first. For example, `freeRelease` is the variant to be managed.

```kotlin
licenseList {
    defaultVariant = "freeRelease"
}
```

And then, run `./gradlew initLicenseList`. It's kinda alias of `initFreeReleaseLicenseList`. For those who need to manage multiple variants, `init<Variant>LicenseList` is available for each variants by default so please use the proper task.

For those who have been using [cookpad/license-tools-plugin](https://github.com/cookpad/license-tools-plugin), you can migrate your licenses.yml. Please check [license-tools-plugin migration](#for-license-tools-plugin-users).

### About management files and syntax

After running `initLicenseList`, you've got the management files below.

- artifact-definition.yml
- license-catalog.yml
- .artifactignore

#### artifact-definitions.yml

This file contains the definitions of artifacts and license *keys* to manage.

**structured with scope (default)**

The base format is `Map<Scope, Map<Group, List<ArtifactDefinition>>>`.

```yaml
"<variant/scope name>":
  "<group>":
    - key: "<artifact name>" # Required
      displayName: "<A name that will be displayed>" # Required
      url: "<the url of this artifact>" # Optional. null or removing this field means this artifact has no url
      copyrightHolders: # Optional. null or removing this field means this artifact has no copyright holders
        - "<copyright holder name>"
        - ...
      license: # Required
        - "<license key which is defined in license-catalog.yml>"
      skip: "<true|false>" # Optional. Specify true if this artifact is not found in the current dependencies but should be displayed. false by default.
    ...
  ...
...
```

<details>

**structured w/o scope**

The base format is `Map<Group, List<ArtifactDefinition>>`.

**flatten**

The base format is `List<ArtifactDefinition>`. The format of `ArtifactDefinition` is almost same but only *key* is different.

```
- key: "<group>:<artifact name>"
  ...
...
```
</details>

#### license-catalog.yml

This file contains licenses that artifacts have references. This plugin infers licenses using their name and provide primary keys automatically.

```yaml
- key: "<the primary key of this license>" # Required
  name: "<name to be displayed>" # Required
  url: "<license url>" # Optional. null or removing this field means this license has no url.
```

#### .artifactignore

This file is to find artifact that should be ignored from the management. Each lines must consist of regular expressions or glob patterns that matches with `<group>:<name>`. If you'd like to use `glob` pattern, you need to configure this plugin through the extension. ref: [Extension](#extension)

**Regular expression samples**

```
com\.example:sample-artifact
io\.github\.jmatsu\.internal:.*
io\.github\.jmatsu\..*
```

**Glob pattern samples**

*: is a path separator instead of /* 

```
com.example:sample-artifact
io.github.jmatsu.internal:*
io.github.jmatsu.**
```

### Manage files

This section explains how you will manage the files that this plugin uses.

#### The basic management cycle

The basic management cycle is the below.

1. Run `./gradlew validate<Variant>LicenseList`
2. Run `merge<Variant>LicenseList` to reflect the current artifacts if failed.
3. Run `./gradlew inspect<Variant>LicenseList`
4. Modify `artifact-definition.yml` based on the inspection report above.
    - You may want to modify `.artifactignore` to exclude several artifacts.
    - If so, please go to Step2 after updating `.artifactignore` to reflect the ignore configurations.
5. And also, modify `license-catalog.yml` as well.
6. Go back to Step1 until no issue is found.

#### Generate a license viewer or its resource

Generate the file for your license viewer by running `./gradlew visualize<Variant>LicenseList` and embed it into your application.

This plugin supports `html` and `json` as the resource format.

## Tasks

This plugin follows the naming strategy of Android Gradle Plugin does as much as possible. i.e. `<actionName><Variant>LicenseList` is it.

### Initialize

`init<Variant>LicenseList`

This is the entrypoint of this plugin. It generates the base definition file and the license catalog file that you will manage.

**Do you want to overwrite it?**

If you would like to re-initialize the definition files, then please pass `-Poverwrite=true` when running this task.

### Inspect

*Available since 0.4*

`inspect<Variant>LicenseList`

Inspect the current management files and report lacked attributes.

NOTE: This doesn't mean your definition file *satisfy* license usages. It's your responsibility, not of this plugin.

### Validate 

`validate<Variant>LicenseList`

This checks if the current definition files and the current project dependencies differ. 

NOTE: This doesn't mean your definition file *satisfy* license usages. It's your responsibility, not of this plugin.

### Merge/Update 

`merge<Variant>LicenseList`

Merge the current project dependencies into the current definition files with respecting the current definition files.

The strategy is *defensive*. This task will preserve your changes in the definition files.

### Visualize

`visualize<Variant>LicenseList`

It will create a HTML file or JSON file based on the plugin configuration if validation and inspection succeed.

NOTE: `-PskipInspect=true` can skip *inspect* action and `-PskipValidate=true` can skip *validate* action.

Tips: [example](./example) renders its licenses based on the both of json and html.

## Extension

```kotlin
licenseList {
    // Control availability of this plugin('s tasks). true by default.
    isEnabled = <true|false>

    // Make initLicenseList an alias of `initFreeReleaseLicenseList`
    defaultTarget = "<variant name like freeRelease>"
    
    // The filter pattern used for ignore feature. regex is default.
    ignoreFormat = "<regex|glob>"

    variants {
        // you can declare the configuration for each variants
        create("freeRelease") {
            // A directory that contains artifact-definition.yml, license-catalog.yml and .artifactignore
            baseDir = file("license-list")

            // options for the management file
            assembly {
                // management file format
                // optional: yaml by default
                format = "<yaml|json>"

                // the style of the managed content
                // optional: structured by default
                style = "<structured|flatten>"

                // whether or not artifacts are grouped by scopes like `implementation`
                // optional: true by default
                groupByScopes = true

                // Rarely used. See Tips/Custom configurations
                // optional
                additionalScopes += setOf("customImplConfiguration")

                // Rarely used. See Tips/WearApp
                // optional
                targetConfigurations += setOf("wearApp")
            }

            // options for the report file
            visualization {
                format = "<html|json>" // html by default

                 // the embedded template will be used by default
                htmlTemplateDir = file("</where/plugin/find/for/html-template>")

                 // To support free maker's breaking changes. rarely used.
                freeMakerVersion = "<version string>"

                // `<variant>/assets` is the default location
                outputDir = file("</where/plugin/generate/file/to>")
            }
        }
    }
}
```

## Tips

### For license-tools-plugin users

ref: [cookpad/license-tools-plugin](https://github.com/cookpad/license-tools-plugin/blob/master/LICENSE.md)

Those who are from license-tools-plugin can migrate their yml file to the format that this plugin supports.

**Configure this plugin for the migration**

```kotlin
plugins {
  id("com.cookpad.android.licensetools") // A migration task is available only when the plugin is applied
  id("io.github.jmatsu.license-list")
}

licenseTools {
  licensesYaml = ... // this property is supported
  ignoreGroups = [...] // this property is also supported
}

licenseList {
  defaultVariant = "<please specify the variant you would like to manage>"
}
```

**Run the migration task**

Please note that the following task is available only when `license-tools-plugin` is applied

```bash
./gradlew migrateLicenseToolsDefinition
```

**Copy generated files and remove license-tools-plugin**

Generated files are available in `/path/to/app-module/build/license-list`. They are `.artifactignore`, `artifact-definition.yml`, and `license-catalog.yml`.
Please move them to the directory where you would like to use for the management. The default configuration will check `/path/to/app-module` directory.

- Only v1.7.0 is tested. Please feel free to open issues if you have any problems.
- `licenseTools.ignoreProjects` is not supported. Because I couldn't imagine the usecase that we really want to ignore *projects*. The group/artifact ignore feature is enough.

*Points*

- Each line of `.artifactignore` are the same to `skip` in license-tools-plugin
- `skip` in `artifact-definition.yml` is the same to `forceGenerate`

### Exclude specific groups/artifacts

You can exclude specific groups and/or artifacts through `.artifactignore` file. `.artifactignore` file is a list of Regexp that matches with `<group>:<name>`.

Let's say you want to exclude `com.example` group and `io.github.jmatsu:example` artifact. Your ignore file should be like the following.

```
com.example:.*
io.github.jmatsu:example
```

Please note that this plugin will automatically add `^` and `$` to each lines, so you must not add it in this ignore file.

For the more details, see [example/app/license-list/.artifactignore](example/app/license-list/.artifactignore).

#### Additional configurations like WearApp

For example `wearApp` is an independent from `implementation` etc. This plugin allows you to add such independent configurations to discovery targets. (No action is required if the variant's configurations extend the custom configurations.)

```kotlin
assembly {
    targetConfigurations += "wearApp"
}
```

### Custom variant aware configurations

If you have created `functionalImplementation` and `<variant>FunctionalImplementation` for each variants, `additionalScopes` will be your help.

```kotlin
assembly {
    // Elements in additionalScopes will be used to build dynamic configurations with using targetConfigurations
    // e.g. `functional` will be used to build `functionalImplementation`, `functionalTestImplementation` and so on
    additionalScopes += "functional"
}
```

### Html Customization

This plugin uses FreeMaker to generate HTML files and can accept an user-defined template like the following.

```kotlin
visualization {
    // the name of the template file must be *license.html.ftl
    htmlTemplateDir = file("customTemplateDir")
}
```

Please check the original `ftl` file for variables that you can use.

### Render Json

You can generate resources in json format.

```kotlin
licenseList {
  variants {
    freeRelease {
            visualization {
                format = "json"
            }
        }
    }
}
```

The schema of the json resources are defined in `schema` module, which is published to jcenter.

```kotlin
repositories {
  jcenter()
}

dependencies {
  implementation("io.github.jmatsu:license-list-schema:<version>")
}
```

They are just *interfaces* in pure Kotlin. So you can chose any serialization method, custom attribute transformation, and so on in the both of Java and Kotlin.

## Limitations

- Only for Android application projects.
    - I think Java project support should also be supported but not yet planned.
- Sharing configuration between variants
- Modification detection

## Migration

### 0.4 breaking changes

Breaking change1 :

`inspect` action was introduced in `0.4`. It reports missing or misconfigured attributes in your management files. `inspect` action does

- Recognize empty string and empty array as *invalid* values, and  null as *valid* values.
  - *null* was default values of `License#url` until `0.3`. Since `0.4`, *""* (empty string) is a default value because `inspection` should fail if no url is found in a pom file. Please modify *null* to *""* in your management file if the *null* is not unintended to make it an inspection target. It's okay to leave `null` as it is if it's intended of course.
  - *empty array* was default values of `ArtifactDefinition#copyrightHolders`. It has not been changed in `0.4` but *empty array* becomes one of invalid values. This means `inspect` will fail anyway. Please use *null* or remove the copyrightHolders field from your management file if the artifact really has no copyright holders.

Breaking change2 :

- Now `visualize` action depends on `validate` and `inspect` actions. 
- Please pass `-PskipInspect=true` and/or `-PskipValidate=true` unless necessary.

Deprecation:

- `artifactOutputDirectory` has been deprecated and renamed to `baseDir`

## License

Under MIT License.

```
 Copyright 2020 Jumpei Matsuda (jmatsu)
```

- This plugin partially uses [cookpad/license-tools-plugin](https://github.com/cookpad/license-tools-plugin/blob/master/LICENSE.md)'s source code to support migration.
- The Gradle plugin design is inspired by [Triple-T/gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher/blob/master/LICENSE).
