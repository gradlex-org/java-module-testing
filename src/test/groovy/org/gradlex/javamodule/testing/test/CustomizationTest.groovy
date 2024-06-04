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
            dependencies { testImplementation(project(path)) }
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

    def "can use task lock service"() {
        given:
        appBuildFile.text = 'import org.gradlex.javamodule.testing.TaskLockService\n\n' + appBuildFile.text
        appBuildFile << '''
            javaModuleTesting.whitebox(testing.suites.getByName<JvmTestSuite>("test") {
                targets.all {
                    testTask {
                        usesService(gradle.sharedServices.registerIfAbsent(TaskLockService.NAME, TaskLockService::class) { maxParallelUsages = 1 })
                    }
                }
            }) {
                requires.add("org.junit.jupiter.api")
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

    def "build does not fail when JUnit has no version and the test folder is empty"() {
        given:
        appTestModuleInfoFile.parentFile.deleteDir()
        appBuildFile << '''
            testing.suites.withType<JvmTestSuite>().all {
                useJUnitJupiter("") // <- no version, we want to manage that ourselves
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.NO_SOURCE
    }

    def "build does not fail when JUnit has no version, the test folder is empty and whitebox was manually configured"() {
        given:
        appTestModuleInfoFile.parentFile.deleteDir()
        appBuildFile << '''
            testing.suites.withType<JvmTestSuite>().all {
                useJUnitJupiter("") // <- no version, we want to manage that ourselves
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.NO_SOURCE
    }
}
