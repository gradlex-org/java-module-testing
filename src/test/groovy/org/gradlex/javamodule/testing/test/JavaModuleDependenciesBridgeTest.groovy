package org.gradlex.javamodule.testing.test

import org.gradle.testkit.runner.TaskOutcome
import org.gradlex.javamodule.testing.test.fixture.GradleBuild
import spock.lang.Ignore
import spock.lang.Specification

class JavaModuleDependenciesBridgeTest extends Specification {

    @Delegate
    GradleBuild build = new GradleBuild()

    def "respects moduleNameToGA mappings"() {
        given:
        appBuildFile << '''
            javaModuleDependencies {
                moduleNameToGA.put("com.example.lib", "com.example:lib")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("com.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
        '''
        appModuleInfoFile << '''
            module com.example.app { 
            }
        '''
        libModuleInfoFile << '''
            module com.example.lib { 
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:test").outcome == TaskOutcome.SUCCESS
    }

    @Ignore // See: https://github.com/gradlex-org/java-module-testing/issues/3
    def "respects moduleNamePrefixToGroup mappings"() {
        given:
        appBuildFile << '''
            javaModuleDependencies {
                moduleNamePrefixToGroup.put("com.example.", "com.example")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("com.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
        '''
        appModuleInfoFile << '''
            module com.example.app { 
            }
        '''
        libModuleInfoFile << '''
            module com.example.lib { 
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
                moduleNameToGA.put("com.example.lib", "com.example:lib")
            }
            javaModuleTesting.whitebox(testing.suites["test"]) {
                requires.add("org.junit.jupiter.api")
                requires.add("com.example.lib")
                opensTo.add("org.junit.platform.commons")
            }
        '''
        appModuleInfoFile << '''
            module com.example.app {
                requires org.slf4j;
                requires /*runtime*/ org.slf4j.simple;
            }
        '''
        libModuleInfoFile << '''
            module com.example.lib { 
            }
        '''

        when:
        def result = runTests()

        then:
        result.task(":app:compileTestJava").outcome == TaskOutcome.SUCCESS
    }
}
