package org.gradlex.javamodule.testing.test

import org.gradle.testkit.runner.TaskOutcome
import org.gradlex.javamodule.testing.test.fixture.GradleBuild
import spock.lang.Specification

class CoreFunctionailtyTest extends Specification {

    @Delegate
    GradleBuild build = new GradleBuild()

    def "testCompileOnly extends compileOnly for whitebox test suites"() {
        given:
        appBuildFile << '''
            javaModuleTesting.classpath(testing.suites["test"])
            dependencies {
                compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
            }
        '''
        file("app/src/main/java/org/example/app/ServletImpl.java") << '''
            package org.example.app;
            public abstract class ServletImpl implements jakarta.servlet.Servlet { }
        '''
        file("app/src/test/java/org/example/app/test/ServletMock.java") << '''
            package org.example.app.test;
            public abstract class ServletMock extends org.example.app.ServletImpl { }
        '''
        appModuleInfoFile << '''
            module org.example.app {
                requires static jakarta.servlet;
            }
        '''

        when:
        def result = runner('compileTestJava').build()

        then:
        result.task(':app:compileTestJava').outcome == TaskOutcome.SUCCESS
    }
}
