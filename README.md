# License List Plugin

License List Plugin is a Gradle plugin to manage artifacts' licenses that your Android project uses. It can generate the data source as human readable or handy format.

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
        copyrightHolders: []
        licenses:
          - apache-2.0
```

This plugin can generate HTML or a json file for the license viewer based on the management file.

Sample view using json | The default html layout
:---|:---
<img src="./assets/recyclerViewSample.png" width="300"> | <img src="./assets/webViewSample.png" width="300">

## Getting Started

NOTE: a sample project is [available](./example).

FIXME: TBW about classpath

### Installation

### Configure your project

Apply the plugin to "com.android.application" modules.

**Kotlin**

```kotlin
plugins {
  id("com.android.application")
  id("license-list") version ("<version}>")
}
```

Other setup is in the following fold.

<details>

**Groovy**

```groovy
plugins {
  id "com.android.application"
  id "license-list" version "<version}>"
}
```

**Non plugins block**

**Kotlin**

```kotlin
// after applying "com.android.application"
apply(plugin = "license-list")
```

**Groovy**

```groovy
// after applying "com.android.application"
apply id: "license-list"
```

</details>

#### Create a management file

You can generate a management file based on the current dependencies.

```bash
./gradlew init<Variant>LicenseList
```


### Tasks

This plugin follows the naming strategy of Android Gradle Plugin does as much as possible. i.e. `<actionName><Variant>LicenseList` is it.

#### `init<Variant>LicenseList`

This is the entrypoint of this plugin. It generates the base definition file and the license catalog file that you will manage.

**Do you want to overwrite it?**

If you would like to re-initialize the definition files, then please pass `-Poverwrite=true` when running this task. 

#### `validate<Variant>LicenseList`

This checks if the current definition files and the current project dependencies differ.

#### `merge<Variant>LicenseList`

Merge the current project dependencies into the current definition files with respecting the current definition files.

The strategy is defensive to preserve your changes in the definition files.

#### `visualize<Variant>LicenseList`

It will create a HTML file or JSON file based on the plugin configuration. 

NOTE: [example](./example) renders its licenses based on the both of json and html.

### Extension

```kotlin
licenseList {
    // initLicenseList will be kinda alias of `initFreeReleaseLicenseList`
    defaultTarget = <variant name like freeRelease>

    variants {
        // you can declare the configuration for each variants
        create("freeRelease") {
            // A directory that contains artifact-definitions.yml and license-catalog.yml
            artifactDefinitionDirectory = file("license-list")

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

                // A path to ignore file.
                // optional: project.file(".artifactignore") by default
                ignoreFile = file(".customartifactignore")
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
```

### Tips

#### For license-tools-plugin users

ref: [cookpad/license-tools-plugin](https://github.com/cookpad/license-tools-plugin/blob/master/LICENSE.md)

Those who are from license-tools-plugin can migrate their yml file to the format that this plugin supports.

**Configure this plugin for the migration**

```kotlin
plugins {
  id("com.cookpad.android.licensetools") // A migration task is available only when the plugin is applied
  id("license-list")
}

licenseTools {
  licensesYaml = ... // this propery is supported
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

#### Additional configurations like WearApp

Add `wearApp` to configurations that will be discovered.

```kotlin
assembly {
    targetConfigurations += "wearApp"
}
```

#### Custom configurations

No action is required if the variant's configurations extend the custom configurations. 

Let's say you have created a `functionalTestImplementation` configuration and tweaked it like the following.

```kotlin
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))
```

`functionalTestImplementation` is a root configuration. Other configurations do not include it. It means `functionalTestImplementation` is not a discovery target.

This plugin supports such an independent configuration as well. There are two patterns to resolve `functionalTestImplementation`.

```kotlin
assembly {
    // Elements in additionalScopes will be used to build dynamic configurations with using targetConfigurations
    // e.g. `functional` will be used to build `functionalImplementation`, `functionalTestImplementation` and so on
    additionalScopes += "functional"
    // or add functionalTestImplementation as one of configurations
    targetConfigurations += "functionalTestImplementation"
}
```

#### Html Customization

This plugin uses FreeMaker to generate HTML files and can accept an user-defined template like the following.

```kotlin
visualization {
    // the name of the template file must be *license.html.ftl
    htmlTemplateDir = file("customTemplateDir")
}
```

Please check the original `ftl` file for variables that you can use.

## Limitations

- Only for Android application projects.
    - I think Java project support should also be supported but not yet planned.
- Sharing configuration between variants
- Modification detection

## Contributing

TBW

## License

Under MIT License.

```
 Copyright 2020 Jumpei Matsuda (jmatsu)
```

- This plugin partially uses [cookpad/license-tools-plugin](https://github.com/cookpad/license-tools-plugin/blob/master/LICENSE.md)'s source code to support migration.
- The Gradle plugin design is inspired by [Triple-T/gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher/blob/master/LICENSE).
