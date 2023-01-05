plugins {
    id("org.my.gradle.java-module")
    id("application")
}

application {
    applicationDefaultJvmArgs = listOf("-ea")
    mainClass.set("org.my.app.App")
    mainModule.set("org.my.app")
}

dependencies {
    implementation(project(":lib"))
    implementation("org.apache.xmlbeans:xmlbeans")
    implementation("org.slf4j:slf4j-api")

    integtestImplementation(project(path))

    runtimeOnly("org.slf4j:slf4j-simple")
}
