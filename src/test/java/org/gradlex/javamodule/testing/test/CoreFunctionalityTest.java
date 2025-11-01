// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testkit.runner.TaskOutcome;
import org.gradlex.javamodule.testing.test.fixture.GradleBuild;
import org.junit.jupiter.api.Test;

class CoreFunctionalityTest {

    GradleBuild build = new GradleBuild();

    @Test
    void testCompileOnly_extends_compileOnly_for_whitebox_test_suites() {
        build.appBuildFile.appendText(
                """
            javaModuleTesting.classpath(testing.suites["test"])
            dependencies {
                compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
            }""");
        build.file("app/src/main/java/org/example/app/ServletImpl.java")
                .writeText(
                        """
            package org.example.app;
            public abstract class ServletImpl implements jakarta.servlet.Servlet { }
            """);
        build.file("app/src/test/java/org/example/app/test/ServletMock.java")
                .writeText(
                        """
            package org.example.app.test;
            public abstract class ServletMock extends org.example.app.ServletImpl { }
            """);
        build.appModuleInfoFile.writeText(
                """
            module org.example.app {
                requires static jakarta.servlet;
            }
            """);

        var result = build.runner("compileTestJava").build();
        var compileTestResult = result.task(":app:compileTestJava");

        assertThat(compileTestResult).isNotNull();
        assertThat(compileTestResult.getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }
}
