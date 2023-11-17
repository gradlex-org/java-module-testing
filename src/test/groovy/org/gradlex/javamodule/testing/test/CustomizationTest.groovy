package org.gradlex.javamodule.testing.test

import org.gradle.testkit.runner.TaskOutcome
import org.gradlex.javamodule.testing.test.fixture.GradleBuild
import spock.lang.Specification

class CustomizationTest extends Specification {

    @Delegate
    GradleBuild build = new GradleBuild()

    def "can customize whitebox test suites in multiple steps"() {
        given:
        appBuildFile << '''
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                opensTo.add("org.junit.platform.commons")
            }
        '''
        appModuleInfoFile << '''
            module org.example.app {
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }

    def "can define whitebox test suite requires in module-info file"() {
        given:
        appModuleInfoFile << '''
            module org.example.app {
            }
        '''
        appWhiteboxTestModuleInfoFile << '''
            module org.example.app.test {
                requires org.example.app;
                requires org.junit.jupiter.api;
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }

    def "repetitive blackbox calls on the same test suite have no effect"() {
        given:
        appBuildFile << '''
            javaModuleTesting.blackbox(testing.suites["test"])
            javaModuleTesting.blackbox(testing.suites["test"])
        '''
        appModuleInfoFile << '''
            module org.example.app {
                exports org.example.app;
            }
        '''
        appTestModuleInfoFile << '''
            open module org.example.app.test { 
                requires org.example.app;
                requires org.junit.jupiter.api;
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }
}
