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
    boolean canUseProjectIsolation = gradleVersionUnderTest == null

    GradleBuild(File projectDir = Files.createTempDirectory("gradle-build").toFile()) {
        this.projectDir = projectDir
        this.settingsFile = file("settings.gradle.kts")
        this.appBuildFile = file("app/build.gradle.kts")
        this.appModuleInfoFile = file("app/src/main/java/module-info.java")
        this.appTestModuleInfoFile = file("app/src/test/java/module-info.java")
        this.appWhiteboxTestModuleInfoFile = file("app/src/test/java9/module-info.java")
        this.libBuildFile = file("lib/build.gradle.kts")
        this.libModuleInfoFile = file("lib/src/main/java/module-info.java")

        def launcherDependency = gradleVersionUnderTest == '7.4' ?
                'testRuntimeOnly("org.junit.platform:junit-platform-launcher")' : ''

        settingsFile << '''
            pluginManagement {
                plugins { id("org.gradlex.java-module-dependencies") version "1.8" }
            }
            dependencyResolutionManagement { repositories.mavenCentral() }
            includeBuild(".")
            rootProject.name = "test-project"
            include("app", "lib")
        '''
        appBuildFile << """
            plugins {
                id("org.gradlex.java-module-testing")
                id("application")
            }
            group = "org.example"
            dependencies {
                testImplementation(platform("org.junit:junit-bom:5.9.0"))
                $launcherDependency 
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
                    System.out.println("Main Module: " + Main.class.getModule().getName());
                    System.out.println("Test Module: " + MainTest.class.getModule().getName());
                }
            }
        '''

        libBuildFile << '''
            plugins {
                id("org.gradlex.java-module-testing")
                id("java-library")
            }
            group = "org.example"
            tasks.withType<JavaCompile>().configureEach { options.release.set(11) }
        '''
    }

    void useJavaModuleDependenciesPlugin() {
        canUseProjectIsolation = false // 'java-module-dependencies' not yet fully compatible
        appBuildFile.text = appBuildFile.text.replace('plugins {', 'plugins { id("org.gradlex.java-module-dependencies")')
        libBuildFile.text = libBuildFile.text.replace('plugins {', 'plugins { id("org.gradlex.java-module-dependencies")')
    }

    def useTestFixturesPlugin() {
        appBuildFile.text = appBuildFile.text.replace('plugins {', 'plugins { id("java-test-fixtures");')
        libBuildFile.text = libBuildFile.text.replace('plugins {', 'plugins { id("java-test-fixtures");')
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
        runner(':app:test').build()
    }

    BuildResult fail() {
        runner('build').buildAndFail()
    }

    GradleRunner runner(String... args) {
        List<String> latestFeaturesArgs = canUseProjectIsolation ? [
                '-Dorg.gradle.unsafe.isolated-projects=true'
        ] : []
        GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withProjectDir(projectDir)
                .withArguments(Arrays.asList(args) + latestFeaturesArgs + '-s' + '--configuration-cache')
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")).with {
            gradleVersionUnderTest ? it.withGradleVersion(gradleVersionUnderTest) : it
        }
    }
}
