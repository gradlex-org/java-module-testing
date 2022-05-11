plugins {
    id("org.my.gradle.java-module")
    id("java-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind")

    integtestImplementation(project(path))
}
