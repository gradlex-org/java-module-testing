plugins {
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.16.0"
}


group = "de.jjohannes.gradle"
version = "0.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

dependencies {
    testImplementation("org.gradle.exemplar:samples-check:1.0.0")
}

gradlePlugin {
    plugins {
        create(project.name) {
            id = "de.jjohannes.java-module-testing"
            implementationClass = "de.jjohannes.gradle.moduletesting.JavaModuleTestingPlugin"
            displayName = "Java Module Testing"
            description = "A plugin to test Java Modules (whitebox and blackbox) without the hassle."
        }
    }
}

pluginBundle {
    website = "https://github.com/jjohannes/java-module-testing"
    vcsUrl = "https://github.com/jjohannes/java-module-testing"
    tags = listOf("java", "modularity", "jigsaw", "jpms", "testing")
}

tasks.test {
    inputs.dir(layout.projectDirectory.dir("samples"))
}
