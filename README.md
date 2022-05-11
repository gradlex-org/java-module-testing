A Gradle 7.4+ plugin to turn a [JVM Test Suite](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:jvm_test_suite_configuration)
into **Blackbox** or **Whitebox** Test Suite for Java Modules.

This plugin is maintained by me, [Jendrik Johannes](https://github.com/jjohannes).
I offer consulting and training for Gradle and/or the Java Module System - please [reach out](mailto:jendrik.johannes@gmail.com) if you are interested.
There is also my [YouTube channel](https://www.youtube.com/playlist?list=PLWQK2ZdV4Yl2k2OmC_gsjDpdIBTN0qqkE) on Gradle topics.

If you have a suggestion or a question, please [open an issue](https://github.com/jjohannes/java-module-testing/issues/new).

# Java Modules with Gradle

If you plan to build Java Modules with Gradle, you should consider using these plugins on top of Gradle core:

- [`id("de.jjohannes.java-module-dependencies")`](https://github.com/jjohannes/java-module-dependencies)  
  Avoid duplicated dependency definitions and get your Module Path under control
- [`id("de.jjohannes.java-module-testing")`](https://github.com/jjohannes/java-module-testing)  
  Proper test setup for Java Modules
- [`id("de.jjohannes.extra-java-module-info")`](https://github.com/jjohannes/extra-java-module-info)  
  Only if your (existing) project cannot avoid using non-module legacy Jars

[Here is a sample](https://github.com/jjohannes/java-module-testing/tree/main/samples/use-all-java-module-plugins)
that shows all plugins in combination.

# How to use?

For a quick start, you can find some samples here:
* [samples/use-all-java-module-plugins](samples/use-all-java-module-plugins)
* [samples/use-only-java-module-testing-plugin](samples/use-only-java-module-testing-plugin)

For general information about how to structure Gradle builds and apply community plugins like this one to all subprojects
you can check out my [Understanding Gradle video series](https://www.youtube.com/playlist?list=PLWQK2ZdV4Yl2k2OmC_gsjDpdIBTN0qqkE).

## Plugin dependency

Add this to the build file of your convention plugin's build
(e.g. `build-logic/build.gradle(.kts)` or `buildSrc/build.gradle(.kts)`).

```
dependencies {
    implementation("de.jjohannes.gradle:java-module-testing:0.1")
}
```

## Apply the plugin

In your convention plugin, apply the plugin.

```
plugins {
    id("de.jjohannes.java-module-testing")
}
```

## Configure a Blackbox Test Suite

To turn the existing JVM Test Suite _integtest_ ito a Blackbox Test Suite:

```
javaModuleTesting.blackbox(testing.suites["integtest"])
```

You can create and/or configure a different test suite as long as you wrap it in `javaModuleTesting.blackbox(...)`.
See documentation on [JVM Test Suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:jvm_test_suite_configuration)
for more details.

It is expected that a blackbox _test source set_ has its own `module-info.java`.
A blackbox test suite is a separate module itself.

## Configure a Whitebox Test Suite

To turn the existing JVM Test Suite _test_ ito a Whitebox Test Suite:

```
javaModuleTesting.whitebox(testing.suites["test"]) {
    requires.add("org.junit.jupiter.api")
    opensTo.add("org.junit.platform.commons")
}
```

You can create and/or configure a different test suite as long as you wrap it in `javaModuleTesting.whitebox(...)`.
See documentation on [JVM Test Suites](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#sec:jvm_test_suite_configuration)
for more details.

It is expected that a whitebox _test source set_ does **not** have a `module-info.java`.
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



