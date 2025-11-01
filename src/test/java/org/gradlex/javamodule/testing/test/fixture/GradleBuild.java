// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test.fixture;

import static java.util.function.Function.identity;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

public class GradleBuild {
    public final Directory projectDir;
    public final WritableFile settingsFile;
    public final WritableFile appBuildFile;
    public final WritableFile appModuleInfoFile;
    public final WritableFile appTestModuleInfoFile;
    public final WritableFile appWhiteboxTestModuleInfoFile;
    public final WritableFile libBuildFile;
    public final WritableFile libModuleInfoFile;

    public static final String GRADLE_VERSION_UNDER_TEST = System.getProperty("gradleVersionUnderTest");

    public GradleBuild() {
        this(createBuildTmpDir());
    }

    public GradleBuild(Path dir) {
        this.projectDir = new Directory(dir);
        this.settingsFile = new WritableFile(projectDir, "settings.gradle.kts");
        this.appBuildFile = new WritableFile(projectDir.dir("app"), "build.gradle.kts");
        this.libBuildFile = new WritableFile(projectDir.dir("lib"), "build.gradle.kts");
        this.appModuleInfoFile = new WritableFile(projectDir.dir("app/src/main/java"), "module-info.java");
        this.libModuleInfoFile = new WritableFile(projectDir.dir("lib/src/main/java"), "module-info.java");
        this.appTestModuleInfoFile = new WritableFile(projectDir, "app/src/test/java/module-info.java");
        this.appWhiteboxTestModuleInfoFile = new WritableFile(projectDir, "app/src/test/java9/module-info.java");

        var launcherDependency = Objects.equals(GRADLE_VERSION_UNDER_TEST, "7.4")
                ? "testRuntimeOnly(\"org.junit.platform:junit-platform-launcher\")"
                : "";

        settingsFile.writeText(
                """
            pluginManagement {
                plugins { id("org.gradlex.java-module-dependencies") version "1.10" }
            }
            dependencyResolutionManagement { repositories.mavenCentral() }
            includeBuild(".")
            rootProject.name = "test-project"
            include("app", "lib")
            """);
        appBuildFile.writeText(
                """
            plugins {
                id("org.gradlex.java-module-testing")
                id("application")
            }
            group = "org.example"
            dependencies {
                testImplementation(platform("org.junit:junit-bom:5.9.0"))
                %s
            }
            application {
                mainModule.set("org.example.app")
                mainClass.set("org.example.app.Main")
            }
            tasks.test {
                testLogging.showStandardStreams = true
            }
            tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
            """
                        .formatted(launcherDependency));
        file("app/src/main/java/org/example/app/Main.java")
                .writeText(
                        """
            package org.example.app;

            public class Main {
                public void main(String... args) {
                }
            }
            """);
        file("app/src/test/java/org/example/app/test/MainTest.java")
                .writeText(
                        """
            package org.example.app.test;

            import org.junit.jupiter.api.Test;
            import org.example.app.Main;

            public class MainTest {

                @Test
                void testApp() {
                    new Main();
                    System.out.println("Main Module: " + Main.class.getModule().getName());
                    System.out.println("Test Module: " + MainTest.class.getModule().getName());
                }
            }
            """);

        libBuildFile.writeText(
                """
            plugins {
                id("org.gradlex.java-module-testing")
                id("java-library")
            }
            group = "org.example"
            tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
            """);
    }

    public GradleBuild useJavaModuleDependenciesPlugin() {
        appBuildFile.writeText(
                appBuildFile.text().replace("plugins {", "plugins { id(\"org.gradlex.java-module-dependencies\")"));
        libBuildFile.writeText(
                libBuildFile.text().replace("plugins {", "plugins { id(\"org.gradlex.java-module-dependencies\")"));
        return this;
    }

    public GradleBuild useTestFixturesPlugin() {
        appBuildFile.writeText(appBuildFile.text().replace("plugins {", "plugins { id(\"java-test-fixtures\");"));
        libBuildFile.writeText(libBuildFile.text().replace("plugins {", "plugins { id(\"java-test-fixtures\");"));
        return this;
    }

    public WritableFile file(String path) {
        return new WritableFile(projectDir, path);
    }

    public BuildResult build() {
        return runner("build").build();
    }

    public BuildResult run() {
        return runner("run").build();
    }

    public BuildResult runTests() {
        return runner(":app:test").build();
    }

    public BuildResult fail() {
        return runner("build").buildAndFail();
    }

    public GradleRunner runner(String... args) {
        boolean debugMode = ManagementFactory.getRuntimeMXBean()
                .getInputArguments()
                .toString()
                .contains("-agentlib:jdwp");
        List<String> latestFeaturesArgs = GRADLE_VERSION_UNDER_TEST != null || debugMode
                ? List.of()
                : List.of(
                        "--configuration-cache",
                        "-Dorg.gradle.unsafe.isolated-projects=true",
                        // "getGroup" in "JavaModuleDependenciesExtension.create"
                        "--configuration-cache-problems=warn",
                        "-Dorg.gradle.configuration-cache.max-problems=4");
        Stream<String> standardArgs = Stream.of("-s", "--warning-mode=fail");
        GradleRunner runner = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withDebug(debugMode)
                .withProjectDir(projectDir.getAsPath().toFile())
                .withArguments(Stream.of(Arrays.stream(args), latestFeaturesArgs.stream(), standardArgs)
                        .flatMap(identity())
                        .collect(Collectors.toList()));
        if (GRADLE_VERSION_UNDER_TEST != null) {
            runner.withGradleVersion(GRADLE_VERSION_UNDER_TEST);
        }
        return runner;
    }

    private static Path createBuildTmpDir() {
        try {
            return Files.createTempDirectory("gradle-build");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
