package org.gradlex.javamodule.testing.test.fixture

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

import java.lang.management.ManagementFactory
import java.nio.file.Files

class GradleBuild {

    final File projectDir
    final File settingsFile
    final File appBuildFile
    final File appModuleInfoFile
    final File appTestModuleInfoFile
    final File appWhiteboxTestModuleInfoFile
    final File libBuildFile
    final File libModuleInfoFile

    final String gradleVersionUnderTest = System.getProperty("gradleVersionUnderTest")

    GradleBuild(File projectDir = Files.createTempDirectory("gradle-build").toFile()) {
        this.projectDir = projectDir
        this.settingsFile = file("settings.gradle.kts")
        this.appBuildFile = file("app/build.gradle.kts")
        this.appModuleInfoFile = file("app/src/main/java/module-info.java")
        this.appTestModuleInfoFile = file("app/src/test/java/module-info.java")
        this.appWhiteboxTestModuleInfoFile = file("app/src/test/java9/module-info.java")
        this.libBuildFile = file("lib/build.gradle.kts")
        this.libModuleInfoFile = file("lib/src/main/java/module-info.java")

        settingsFile << '''
            pluginManagement {
                plugins { id("org.gradlex.java-module-dependencies") version "1.3" }
            }
            dependencyResolutionManagement { repositories.mavenCentral() }
            includeBuild(".")
            rootProject.name = "test-project"
            include("app", "lib")
        '''
        appBuildFile << '''
            plugins {
                id("org.gradlex.java-module-dependencies")
                id("org.gradlex.java-module-testing")
                id("application")
            }
            group = "org.example"
            dependencies {
                testImplementation(platform("org.junit:junit-bom:5.9.0"))
            }
            application {
                mainModule.set("org.example.app")
                mainClass.set("org.example.app.Main")
            }
        '''
        file("app/src/main/java/org/example/app/Main.java") << '''
            package org.example.app;
            
            public class Main {
                public void main(String... args) {
                }
            }
        '''
        file("app/src/test/java/org/example/app/test/MainTest.java") << '''
            package org.example.app.test;
            
            import org.junit.jupiter.api.Test;
            import org.example.app.Main;
            
            public class MainTest {
                
                @Test
                void testApp() {
                    new Main();
                }
            }
        '''

        libBuildFile << '''
            plugins {
                id("org.gradlex.java-module-dependencies")
                id("org.gradlex.java-module-testing")
                id("java-library")
            }
            group = "org.example"
        '''
    }

    File file(String path) {
        new File(projectDir, path).tap {
            it.getParentFile().mkdirs()
        }
    }

    BuildResult build() {
        runner('build').build()
    }

    BuildResult run() {
        runner('run').build()
    }

    BuildResult runTests() {
        runner(':app:test', '-q').build()
    }

    BuildResult fail() {
        runner('build').buildAndFail()
    }

    GradleRunner runner(String... args) {
        GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments(Arrays.asList(args) + '-s')
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")).with {
            gradleVersionUnderTest ? it.withGradleVersion(gradleVersionUnderTest) : it
        }
    }
}
