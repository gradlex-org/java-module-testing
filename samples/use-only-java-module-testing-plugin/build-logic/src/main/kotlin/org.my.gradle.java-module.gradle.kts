plugins {
    id("java")
    id("org.my.gradle.base")
    id("de.jjohannes.java-module-testing")
}

javaModuleTesting.whitebox(
    testing.suites.getByName<JvmTestSuite>("test") {
        useJUnitJupiter("")
        targets.all {
            testTask { jvmArgs("-Dorg.slf4j.simpleLogger.defaultLogLevel=error") }
        }
    }
) {
    requires.add("org.junit.jupiter.api")
    opensTo.add("org.junit.platform.commons")
}

javaModuleTesting.blackbox(
    testing.suites.create<JvmTestSuite>("integtest") {
        useJUnitJupiter("")
        testType.set("blackbox")
        dependencies {
            implementation(project.dependencies.platform(project(":platform")))
        }
        targets.all {
            testTask { jvmArgs("-Dorg.slf4j.simpleLogger.defaultLogLevel=error") }
        }
        tasks.check { dependsOn(targets) }
    }
)

dependencies {
    implementation(platform(project(":platform")))
}
