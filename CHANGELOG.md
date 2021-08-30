## Version 0.8.0 (2021/08/30)

**Improvements** :tada:

- Support Gradle 7.x compatibility. [#48](https://github.com/jmatsu/license-list-plugin/pull/48)

## Version 0.7.0 (2020/09/11)

**Bug fixes** :tada:

- Use github raw content URLs instead of web pages. [#43](https://github.com/jmatsu/license-list-plugin/pull/43)
    - Please run `mergeLicenseList` and `visualizeLicenseList` to follow this change.

## Version 0.6.1 (2020/06/15)

**Bug fixes** :tada:

- Ignore local jar/arr files based on .artifactignore properly. [#40](https://github.com/jmatsu/license-list-plugin/pull/40)

## Version 0.6.0 (2020/06/12)

**New features** :tada:

- Support local jar/aar files. [#36](https://github.com/jmatsu/license-list-plugin/pull/36)

**Bug fixes** :tada:

- Licenses that contain heading/trailing spaces and/or CRLF always caused validation errors. [#37](https://github.com/jmatsu/license-list-plugin/pull/37)

## Version 0.5.0 (2020/04/24)

**New features** :tada:

- `glob` pattern is available to ignore artifacts. [#34](https://github.com/jmatsu/license-list-plugin/pull/34)

**Bug fixes**

- `validation` task didn't look up `keep` attributes [#33](https://github.com/jmatsu/license-list-plugin/pull/33)

## Version 0.4.1 (2020/04/08)

**Deprecation** :bow:

- dataDir has been deprecated and please use baseDir. This was a typo. [#31](https://github.com/jmatsu/license-list-plugin/pull/31)

## Version 0.4 (2020/04/08)

**New features** :tada:

- `inspect` action is introduced. It reports missing or misconfigured attributes in your management files. [#28](https://github.com/jmatsu/license-list-plugin/pull/28)

**Breaking changes** :bomb:

- `inspect` action recognizes null as valid values but null was default values of License#url until 0.3. Since 0.4, "" (empty string) is a default value. [#28](https://github.com/jmatsu/license-list-plugin/pull/28)
- Now visualize action depends on validate and inspect actions. [#28](https://github.com/jmatsu/license-list-plugin/pull/28)

**Deprecation** :bow:

- artifactOutputDirectory has been deprecated and renamed to baseDir [#26](https://github.com/jmatsu/license-list-plugin/pull/26)

## Version 0.3 (2020/04/07)

- Fixed missing `</body>` in the html template
- Fixed unintended *wider* html page had been generated
- Raise the error in advance when the file of license-tools-plugin is not found

## Version 0.2 (2020/04/07)

- Fixed assembly/visualization options caused missing property errors in Groovy code

## Version 0.1 (2020/04/06)

- The initial version
