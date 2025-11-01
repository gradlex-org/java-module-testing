// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testkit.runner.TaskOutcome;
import org.gradlex.javamodule.testing.test.fixture.GradleBuild;
import org.junit.jupiter.api.Test;

class JavaModuleDependenciesBridgeTest {

    GradleBuild build = new GradleBuild().useJavaModuleDependenciesPlugin();

    @Test
    void respects_moduleNameToGA_mappings() {
        build.appBuildFile.appendText(
                """
            javaModuleDependencies {
                moduleNameToGA.put("org.example.lib", "org.example:lib")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
            """);
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);
        build.libModuleInfoFile.writeText("""
            module org.example.lib {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void respects_moduleNamePrefixToGroup_mappings() {
        build.appBuildFile.appendText(
                """
            javaModuleDependencies {
                moduleNamePrefixToGroup.put("org.example.", "org.example")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
            """);
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);
        build.libModuleInfoFile.writeText("""
            module org.example.lib {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void compiles_with_provides_runtime_directives() {
        build.appBuildFile.appendText(
                """
            dependencies.constraints {
                javaModuleDependencies {
                    implementation(gav("org.slf4j", "2.0.3"))
                    implementation(gav("org.slf4j.simple", "2.0.3"))
                }
            }
            javaModuleDependencies {
                moduleNameToGA.put("org.example.lib", "org.example:lib")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
            """);
        build.appModuleInfoFile.writeText(
                """
            module org.example.app {
                requires org.slf4j;
                requires /*runtime*/ org.slf4j.simple;
            }
            """);
        build.libModuleInfoFile.writeText("""
            module org.example.lib {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void can_be_combined_with_testfixtures_plugin() {
        build.useTestFixturesPlugin();
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);
        build.file("app/src/testFixtures/java/module-info.java")
                .writeText(
                        """
            open module org.example.app.test.fixtures {
                requires org.example.app;
            }
            """);
        build.appBuildFile.appendText(
                """
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.app.test.fixtures")
            }
            javaModuleDependencies {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }
}
