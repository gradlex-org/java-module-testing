package org.gradlex.javamodule.testing.test

import org.gradle.testkit.runner.TaskOutcome
import org.gradlex.javamodule.testing.test.fixture.GradleBuild
import spock.lang.Specification

class ClasspathSuiteTest extends Specification {

    @Delegate
    GradleBuild build = new GradleBuild()

    def "can configure classpath test suite"() {
        given:
        appBuildFile << '''
            javaModuleTesting.classpath(testing.suites["test"])
        '''
        appModuleInfoFile << '''
            module org.example.app {
            }
        '''

        when:
        def result = runTests()

        then:
        result.output.contains('Main Module: null')
        result.output.contains('Test Module: null')
        result.task(':app:test').outcome == TaskOutcome.SUCCESS
    }
}
