// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testkit.runner.TaskOutcome;
import org.gradlex.javamodule.testing.test.fixture.GradleBuild;
import org.junit.jupiter.api.Test;

class ClasspathSuiteTest {

    GradleBuild build = new GradleBuild();

    @Test
    void can_configure_classpath_test_suite() {
        build.appBuildFile.appendText("""
            javaModuleTesting.classpath(testing.suites["test"])
            """);
        build.appModuleInfoFile.appendText("""
            module org.example.app {
            }
            """);

        var result = build.runTests();
        var testResult = result.task(":app:test");

        assertThat(result.getOutput()).contains("Main Module: null");
        assertThat(result.getOutput()).contains("Test Module: null");
        assertThat(testResult).isNotNull();
        assertThat(testResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }
}
