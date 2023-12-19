# Java Module Testing Gradle Plugin - Changelog

## Version 1.3.1
* Improve interoperability with `java-module-dependencies` plugin

## Version 1.3
* [#18](https://github.com/gradlex-org/java-module-testing/issues/18) Allow whitebox tests to define requires in `java9/module-info.java` (Thanks [brianoliver](https://github.com/brianoliver) for suggesting!)

## Version 1.2.2
* No duplicated '--add-opens' runtime args

## Version 1.2.1
* Fix 'module-info.java' parsing bug

## Version 1.2
* [#8](https://github.com/gradlex-org/java-module-testing/issues/8) Automatically configure test suites based on the existence of a `module-info.java` file
* [#5](https://github.com/gradlex-org/java-module-testing/issues/5) Improve module-info parsing

## Version 1.1
* Integrate with https://github.com/gradlex-org/java-module-dependencies/issues/19

## Version 1.0
* Moved project to [GradleX](https://gradlex.org) - new plugin ID: `org.gradlex.java-module-testing`

## Versions 0.1
* Initial features added
