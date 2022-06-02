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

val pluginId = "de.jjohannes.java-module-testing"
val pluginClass = "de.jjohannes.gradle.moduletesting.JavaModuleTestingPlugin"
val pluginName = "Java Module Testing Gradle Plugin"
val pluginDescription = "A plugin to test Java Modules (whitebox and blackbox) without the hassle."
val pluginBundleTags = listOf("java", "modularity", "jigsaw", "jpms", "testing")
val pluginGitHub = "https://github.com/jjohannes/java-module-testing"

gradlePlugin {
    plugins {
        create(project.name) {
            id = pluginId
            implementationClass = pluginClass
            displayName = pluginName
            description = pluginDescription
        }
    }
}

pluginBundle {
    website = pluginGitHub
    vcsUrl = pluginGitHub
    tags = pluginBundleTags
}

publishing {
    publications.withType<MavenPublication>().all {
        pom.name.set(pluginName)
        pom.description.set(pluginDescription)
        pom.url.set(pluginGitHub)
        pom.licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        pom.developers {
            developer {
                id.set("jjohannes")
                name.set("Jendrik Johannes")
                email.set("jendrik@onepiece.software")
            }
        }
        pom.scm {
            url.set(pluginGitHub)
        }
    }
}

tasks.test {
    inputs.dir(layout.projectDirectory.dir("samples"))
}
