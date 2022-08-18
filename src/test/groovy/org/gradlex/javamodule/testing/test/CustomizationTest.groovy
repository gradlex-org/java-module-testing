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
            module org.gradlex.test.app { 
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
            module org.gradlex.test.app { 
            }
        '''
        appTestModuleInfoFile << '''
            module org.gradlex.test.app.test { 
                requires org.junit.jupiter.api;
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }
}
