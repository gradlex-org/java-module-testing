# Java Module Testing Gradle plugin

[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fgradlex-org%2Fjava-module-testing%2Fbadge%3Fref%3Dmain&style=flat)](https://actions-badge.atrox.dev/gradlex-org/java-module-testing/goto?ref=main)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Forg%2Fgradlex%2Fjava-module-testing%2Forg.gradlex.java-module-testing.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/org.gradlex.java-module-testing)

A Gradle 7.4+ plugin to turn a [JVM Test Suite](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:jvm_test_suite_configuration)
into a **Blackbox** or **Whitebox** Test Suite for Java Modules.

This plugin is maintained by me, [Jendrik Johannes](https://github.com/jjohannes).
I offer consulting and training for Gradle and/or the Java Module System - please [reach out](mailto:jendrik.johannes@gmail.com) if you are interested.
There is also my [YouTube channel](https://www.youtube.com/playlist?list=PLWQK2ZdV4Yl2k2OmC_gsjDpdIBTN0qqkE) on Gradle topics.

If you have a suggestion or a question, please [open an issue](https://github.com/gradlex-org/java-module-testing/issues/new).

# Java Modules with Gradle

If you plan to build Java Modules with Gradle, you should consider using these plugins on top of Gradle core:

- [`id("org.gradlex.java-module-dependencies")`](https://github.com/gradlex-org/java-module-dependencies)  
  Avoid duplicated dependency definitions and get your Module Path under control
- [`id("org.gradlex.java-module-testing")`](https://github.com/gradlex-org/java-module-testing)  
  Proper test setup for Java Modules
- [`id("org.gradlex.extra-java-module-info")`](https://github.com/gradlex-org/extra-java-module-info)  
  Only if your (existing) project cannot avoid using non-module legacy Jars

[Here is a sample](https://github.com/gradlex-org/java-module-testing/tree/main/samples/use-all-java-module-plugins)
that shows all plugins in combination.

[Full Java Module System Project Setup](https://github.com/jjohannes/gradle-project-setup-howto/tree/java_module_system) is a full-fledged Java Module System project setup using these plugins.  
[<img src="https://onepiecesoftware.github.io/img/videos/15-3.png" width="260">](https://www.youtube.com/watch?v=uRieSnovlVc&list=PLWQK2ZdV4Yl2k2OmC_gsjDpdIBTN0qqkE)

# How to use?

For a quick start, you can find some samples here:
* [samples/use-all-java-module-plugins](samples/use-all-java-module-plugins)
* [samples/use-only-java-module-testing-plugin](samples/use-only-java-module-testing-plugin)
* [samples/use-with-test-fixtures](samples/use-with-test-fixtures)

For general information about how to structure Gradle builds and apply community plugins like this one to all subprojects
you can check out my [Understanding Gradle video series](https://www.youtube.com/playlist?list=PLWQK2ZdV4Yl2k2OmC_gsjDpdIBTN0qqkE).

## Plugin dependency

Add this to the build file of your convention plugin's build
(e.g. `build-logic/build.gradle(.kts)` or `buildSrc/build.gradle(.kts)`).

```
dependencies {
    implementation("org.gradlex:java-module-testing:1.2.2")
}
```

## Apply the plugin

In your convention plugin, apply the plugin.

```
plugins {
    id("org.gradlex.java-module-testing")
}
```

## Blackbox Test Suites

The plugin automatically turns [JVM Test Suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html) into _Blackbox Test Suites_ if the `src/<test-suite-name>/module-info.java` file exists.
A blackbox test suite is a separate module itself.
See documentation on [JVM Test Suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:jvm_test_suite_configuration) for more details on creating and configuring test suites.

## Whitebox Test Suites

The plugin automatically turns [JVM Test Suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html) **without** `module-info.java` file into _Whitebox Test Suites_.
Whitebox Test Suites might require additional configuration, which can be done like this:

```
javaModuleTesting.whitebox(testing.suites["test"]) {
    requires.add("org.junit.jupiter.api")
    // opensTo.add("org.junit.platform.commons") <-- opensTo 'org.junit.platform.commons' is done by default
}
```

See documentation on [JVM Test Suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:jvm_test_suite_configuration) for more details on creating and configuring test suites.

Alternatively, you can put the `requires` into a `module-info.java` file using the same notation that you would use for blackbox tests.
For this, you need to create the file in `<src-set-location>/java9/module-info.java`. For example:

```
src
  ├── main
  │   └── java
  │       ├── module-info.java
  │       └── ...
  └── test
      ├── java
      │   └── ...
      └── java9
          └── module-info.java
              | module org.example.app.test {
              |   requires org.example.app; // 'main' module into which the tests are patched
              |   requires org.junit.jupiter.api;
              | }
}
```

A whitebox _test source set_ does **not** have a `module-info.java`.
Instead, the _main_ and _test_ classes will be patched together and the test will run in the _main_ module which now includes the test classes as well.
Additional `requires` for the test are defined as shown above.
If the _sources under test_ are located in a different source set (not `main`), this can be configured via `sourcesUnderTest.set("source-set-name")`.

# What does the plugin do?

The plugin rewires the inputs of test compilation (`:compileTestJava`) and test runtime (`:test`).
This includes configuring the Module Path and adding patch parameters in the case of whitebox testing.

## Blackbox Test

Changes for test runtime (`:test`):
- Normally, the test classes are loaded from a `classes` folder
- Now, the test classes are packaged into a module `jar` together with the test resources. Otherwise, test resources would not be visible to the test module at runtime.

## Whitebox Test

Changes for test compilation (`:compileTestJava`):
- Normally, Gradle would not use the Module Path, as there is no `moudle-info.java` in the source set
- Now, a Module Path is computed for the compilation. 
  Using `--patch-module`, the test classes are compiled as an addition to the main module.

Changes for test runtime (`:test`):
- Normally, Gradle would not run its test runner as Module, as there is no `moudle-info.class` as part of the compiled tests.
- Now, the main and test classes are both used as locations for test discovery.
  By this, Gradle will find the `moudle-info.class` of the main module for the tests.
  Using `--patch-module`, _main classes_, _main resources_, _test classes_, and _test resources_ folders are all merged to be treated as one module during test runtime.

# Disclaimer

Gradle and the Gradle logo are trademarks of Gradle, Inc.
The GradleX project is not endorsed by, affiliated with, or associated with Gradle or Gradle, Inc. in any way.
