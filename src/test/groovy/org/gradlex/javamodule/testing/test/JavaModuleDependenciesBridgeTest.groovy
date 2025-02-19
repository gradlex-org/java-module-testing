package org.gradlex.javamodule.testing.test

import org.gradle.testkit.runner.TaskOutcome
import org.gradlex.javamodule.testing.test.fixture.GradleBuild
import spock.lang.Specification

class JavaModuleDependenciesBridgeTest extends Specification {

    @Delegate
    GradleBuild build = new GradleBuild()

    def setup() {
        useJavaModuleDependenciesPlugin()
    }

    def "respects moduleNameToGA mappings"() {
        given:
        appBuildFile << '''
            javaModuleDependencies {
                moduleNameToGA.put("org.example.lib", "org.example:lib")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
        '''
        appModuleInfoFile << '''
            module org.example.app { 
            }
        '''
        libModuleInfoFile << '''
            module org.example.lib { 
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }

    def "respects moduleNamePrefixToGroup mappings"() {
        given:
        appBuildFile << '''
            javaModuleDependencies {
                moduleNamePrefixToGroup.put("org.example.", "org.example")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
        '''
        appModuleInfoFile << '''
            module org.example.app { 
            }
        '''
        libModuleInfoFile << '''
            module org.example.lib { 
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }

    def "compiles with provides runtime directives"() {
        given:
        appBuildFile << '''
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
        '''
        appModuleInfoFile << '''
            module org.example.app {
                requires org.slf4j;
                requires /*runtime*/ org.slf4j.simple;
            }
        '''
        libModuleInfoFile << '''
            module org.example.lib { 
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:compileTestJava").outcome == TaskOutcome.SUCCESS
    }

    def "can be combined with test-fixtures plugins"() {
        given:
        useTestFixturesPlugin()
        appModuleInfoFile << '''
            module org.example.app { 
            }
        '''
        file("app/src/testFixtures/java/module-info.java") << '''
            open module org.example.app.test.fixtures {
                requires org.example.app;
            }
        '''
        appBuildFile << '''
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("org.example.app.test.fixtures")
            }
            javaModuleDependencies {
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }
}
