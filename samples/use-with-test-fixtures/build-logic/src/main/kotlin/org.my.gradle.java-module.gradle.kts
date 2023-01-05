plugins {
    id("java")
    id("java-test-fixtures")
    id("org.my.gradle.base")
    id("org.gradlex.java-module-testing")
}

javaModuleTesting.whitebox(
    testing.suites.getByName<JvmTestSuite>("test") {
        useJUnitJupiter()
        targets.all { testTask { testLogging.showStandardStreams = true } }
    }
) {
    requires.add("org.junit.jupiter.api")
    opensTo.add("org.junit.platform.commons")
    requires.add("org.my.lib.test.fixtures")
}

javaModuleTesting.blackbox(
    testing.suites.create<JvmTestSuite>("integtest") {
        useJUnitJupiter()
        testType.set(TestSuiteType.INTEGRATION_TEST)
        targets.all { testTask { testLogging.showStandardStreams = true } }
        tasks.check { dependsOn(targets) }
    }
)

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.13.2"))
}
