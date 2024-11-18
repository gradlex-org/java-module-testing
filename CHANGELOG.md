# Java Module Testing Gradle Plugin - Changelog

## Version 1.5
* [#47](https://github.com/gradlex-org/java-module-testing/issues/47) Add support for Classpath Test Suites
* [#51](https://github.com/gradlex-org/java-module-testing/issues/51) testCompileOnly extends compileOnly for Whitebox Test Suites
* [#67](https://github.com/gradlex-org/java-module-testing/issues/67) Whitebox Test Suites: add `exportsTo` configuration option

## Version 1.4
* [#2](https://github.com/gradlex-org/java-module-testing/issues/2) New approach to split Module Path and Classpath for whitebox testing
* [#40](https://github.com/gradlex-org/java-module-testing/issues/40) `useJUnitJupiter("")` without version does not fail for empty test directories
* [#39](https://github.com/gradlex-org/java-module-testing/issues/39) Add `TaskLockService` for conveniently running test tasks in isolation

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
