plugins {
    id("java")
    id("org.my.gradle.base")
    id("org.gradlex.java-module-testing")
}

testing.suites.getByName<JvmTestSuite>("test") {
    useJUnitJupiter("")
    targets.all {
        testTask { jvmArgs("-Dorg.slf4j.simpleLogger.defaultLogLevel=error") }
    }
}

testing.suites.create<JvmTestSuite>("integtest") {
    useJUnitJupiter("")
    dependencies {
        implementation(project.dependencies.platform(project(":platform")))
    }
    targets.all {
        testTask { jvmArgs("-Dorg.slf4j.simpleLogger.defaultLogLevel=error") }
    }
    tasks.check { dependsOn(targets) }
}


dependencies {
    implementation(platform(project(":platform")))
}
