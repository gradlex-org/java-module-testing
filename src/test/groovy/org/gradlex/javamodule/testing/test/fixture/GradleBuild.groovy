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

    final String gradleVersionUnderTest = System.getProperty("gradleVersionUnderTest")

    GradleBuild(File projectDir = Files.createTempDirectory("gradle-build").toFile()) {
        this.projectDir = projectDir
        this.settingsFile = file("settings.gradle.kts")
        this.appBuildFile = file("app/build.gradle.kts")
        this.appModuleInfoFile = file("app/src/main/java/module-info.java")
        this.appTestModuleInfoFile = file("app/src/test/java/module-info.java")

        settingsFile << '''
            dependencyResolutionManagement { repositories.mavenCentral() }
            rootProject.name = "test-project"
            include("app")
        '''
        appBuildFile << '''
            plugins {
                id("org.gradlex.java-module-dependencies") version "1.0"
                id("org.gradlex.java-module-testing")
                id("application")
            }
            dependencies {
                testImplementation(platform("org.junit:junit-bom:5.9.0"))
            }
            application {
                mainModule.set("org.gradlex.test.app")
                mainClass.set("org.gradlex.test.app.Main")
            }
            tasks.register("printRuntimeJars") {
                doLast { println(configurations.runtimeClasspath.get().files.map { it.name }) }
            }
            tasks.register("printCompileJars") {
                doLast { println(configurations.compileClasspath.get().files.map { it.name }) }
            }
        '''

        file("app/src/test/java/com/example/AppTest.java") << '''
            package com.example;
            
            import org.junit.jupiter.api.Test;
            
            public class AppTest {
                
                @Test
                void testApp() {
                }
            }
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
