plugins {
    id("java")
    id("java-test-fixtures")
    id("org.my.gradle.base")
    id("org.gradlex.java-module-dependencies")
    id("org.gradlex.java-module-testing")
}

javaModuleTesting.whitebox(
    testing.suites.getByName<JvmTestSuite>("test") {
        targets.all { testTask { testLogging.showStandardStreams = true } }
    }
) {
    requires.add("org.junit.jupiter.api")
    requires.add("org.my.lib.test.fixtures")
}

testing.suites.create<JvmTestSuite>("integtest") {
    targets.all { testTask { testLogging.showStandardStreams = true } }
    tasks.check { dependsOn(targets) }
}

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.19.1"))
}
