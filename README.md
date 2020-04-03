# License List Plugin

License List Plugin is a management plugin for artifacts' licenses that your Android project uses. It can generate the data source as human readable or handy format.

<img src="./assets/recyclerViewSample.png" width="300"> | <img src="./assets/webViewSample.png" width="300">

## Getting Started

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

This plugin follows the naming strategy of Android Gradle Plugin does as much as possible. `<actionName><Variant>`

## Known limitations

- Only for Android application projects.