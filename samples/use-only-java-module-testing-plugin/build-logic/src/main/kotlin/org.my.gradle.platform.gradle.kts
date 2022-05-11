plugins {
    id("java-platform")
    id("org.my.gradle.base")
}

javaPlatform.allowDependencies() // Use existing Platforms/BOMs
