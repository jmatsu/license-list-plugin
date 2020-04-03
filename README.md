# License List Plugin

License List Plugin is a management plugin for artifacts' licenses that your Android project uses. It can generate the data source as human readable or handy format.

<img src="./assets/recyclerViewSample.png" width="300"> | <img src="./assets/webViewSample.png" width="300">

## Getting Started

NOTE: a sample project is [available](./example).

FIXME: TBW about classpath

### Installation

Apply the plugin to "com.android.application" modules.

**Kotlin**

```kotlin
plugins {
  id("com.android.application")
  id("license-list") version ("<version}>")
}
```

**Groovy**

```groovy
plugins {
  id "com.android.application"
  id "license-list" version "<version}>"
}
```

**Non plugins block**

<details>

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

### Tips

#### WearApp

Add `wearApp` to configurations that will be discovered.

```kotlin
licenseList {
    targetConfigurations += "wearApp"
}
```

#### Custom configurations

No action is required if the variant's configurations extend the custom configurations. 

Let's say you have created a `functionalTestImplementation` configuration like the following. 

```kotlin
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))
```

`functionalTestImplementation` is kinda *root* configuration so any other configurations include it. It means `functionalTestImplementation` is not a discovery target.

This plugin supports such an independent configuration as well. There are two patterns to resolve `functionalTestImplementation`.

```kotlin
licenseList {
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
licenseList {
    // the name of the template file must be *license.html.ftl
    htmlTemplateDir = file("customTemplateDir")
}
```

Please check the original `ftl` file for variables that you can use.

## Limitations

- Only for Android application projects.

## Contributing

TBW

## License

Under Apache 2.0 LICENSE.

```
 Copyright 2020 Jumpei Matsuda (jmatsu)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
```

- This plugin partially uses [cookpad/license-tools-plugin](https://github.com/cookpad/license-tools-plugin/blob/master/LICENSE.md)'s source code to support migration.
- The Gradle plugin design is inspired by [Triple-T/gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher/blob/master/LICENSE).
