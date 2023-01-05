plugins {
    id("org.my.gradle.java-module")
    id("java-library")
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")

    integtestImplementation(testFixtures(project(path)))
}
