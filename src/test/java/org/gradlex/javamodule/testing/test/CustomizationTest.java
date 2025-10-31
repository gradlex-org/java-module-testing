// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testkit.runner.TaskOutcome;
import org.gradlex.javamodule.testing.test.fixture.GradleBuild;
import org.junit.jupiter.api.Test;

class CustomizationTest {

    GradleBuild build = new GradleBuild();

    @Test
    void can_customize_whitebox_test_suites_in_multiple_steps() {
        build.appBuildFile.appendText(
                """
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                opensTo.add("org.junit.platform.commons")
            }
            """);
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(result.getOutput()).contains("Main Module: org.example.app");
        assertThat(result.getOutput()).contains("Test Module: org.example.app");
        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void can_change_sourcesUnderTest_of_whitebox_test_suite() {
        build.useTestFixturesPlugin();
        build.file("app/src/testFixtures/java/module-info.java")
                .writeText("""
            module org.example.fixtures {
            }
            """);
        build.file("app/src/testFixtures/java/org/example/app/Main.java")
                .writeText("""
            package org.example.app;
            public class Main {}
            """);
        build.appBuildFile.appendText(
                """
            javaModuleTesting.whitebox(testing.suites["test"]) {
                sourcesUnderTest.set(sourceSets.testFixtures);
                requires.add("org.junit.jupiter.api")
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(result.getOutput()).contains("Main Module: org.example.fixtures");
        assertThat(result.getOutput()).contains("Test Module: org.example.fixtures");
        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void can_define_whitebox_test_suite_requires_in_moduleinfo_file() {
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);
        build.appWhiteboxTestModuleInfoFile.writeText(
                """
            module org.example.app.test {
                requires org.example.app;
                requires org.junit.jupiter.api;
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void can_customize_whitebox_test_suites_with_exportsTo() {
        var mainTest = build.file("app/src/test/java/org/example/app/test/MainTest.java");
        // make test public, so that 'exportsTo org.junit.platform.commons' is sufficient
        mainTest.writeText(mainTest.text().replace("void testApp()", "public void testApp()"));

        build.appBuildFile.appendText(
                """
            javaModuleTesting.classpath(testing.suites["test"]) // reset default setup
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                exportsTo.add("org.junit.platform.commons")
            }
            """);
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(result.getOutput()).contains("Main Module: org.example.app");
        assertThat(result.getOutput()).contains("Test Module: org.example.app");
        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void repetitive_blackbox_calls_on_the_same_test_suite_have_no_effect() {
        build.appBuildFile.appendText(
                """
            javaModuleTesting.blackbox(testing.suites["test"])
            javaModuleTesting.blackbox(testing.suites["test"])
            dependencies { testImplementation(project(path)) }
            """);
        build.appModuleInfoFile.writeText(
                """
            module org.example.app {
                exports org.example.app;
            }
            """);
        build.appTestModuleInfoFile.writeText(
                """
            open module org.example.app.test {
                requires org.example.app;
                requires org.junit.jupiter.api;
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(result.getOutput()).contains("Main Module: org.example.app");
        assertThat(result.getOutput()).contains("Test Module: org.example.app");
        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void can_use_task_lock_service() {
        build.appBuildFile.writeText(
                "import org.gradlex.javamodule.testing.TaskLockService\n\n" + build.appBuildFile.text());
        build.appBuildFile.appendText(
                """
            javaModuleTesting.whitebox(testing.suites.getByName<JvmTestSuite>("test") {
                targets.all {
                    testTask {
                        usesService(gradle.sharedServices.registerIfAbsent(TaskLockService.NAME, TaskLockService::class) { maxParallelUsages.set(1) })
                    }
                }
            }) {
                requires.add("org.junit.jupiter.api")
            }
            """);
        build.appModuleInfoFile.writeText("""
            module org.example.app {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    @Test
    void build_does_not_fail_when_JUnit_has_no_version_and_the_test_folder_is_empty() {
        build.appTestModuleInfoFile.parent().delete();
        build.appBuildFile.appendText(
                """
            testing.suites.withType<JvmTestSuite>().all {
                useJUnitJupiter("") // <- no version, we want to manage that ourselves
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.NO_SOURCE);
    }

    @Test
    void
            build_does_not_fail_when_JUnit_has_no_version_and_the_test_folder_is_empty_and_whitebox_was_manually_configured() {
        build.appTestModuleInfoFile.parent().delete();
        build.appBuildFile.appendText(
                """
            testing.suites.withType<JvmTestSuite>().all {
                useJUnitJupiter("") // <- no version, we want to manage that ourselves
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.NO_SOURCE);
    }
}
