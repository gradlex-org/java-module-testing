plugins {
    id("groovy")
    id("org.gradlex.internal.plugin-publish-conventions") version "0.4"
}

group = "de.jjohannes.gradle"
version = "0.2"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

signing {
    isRequired = false
}

dependencies {
    implementation("org.gradlex:${project.name}:1.0")

    testImplementation("org.spockframework:spock-core:2.1-groovy-3.0")
    testImplementation("org.gradle.exemplar:samples-check:1.0.0")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

pluginPublishConventions {
    id("de.jjohannes.${project.name}")
    implementationClass("de.jjohannes.gradle.moduletesting.JavaModuleTestingPlugin")
    displayName("Java Module Testing Gradle Plugin")
    description("!!! Plugin ID changed to 'org.gradlex.${project.name}' !!!")
    tags("gradlex", "java", "modularity", "jigsaw", "jpms", "testing")
    gitHub("https://github.com/gradlex-org/java-module-testing")
    developer {
        id.set("jjohannes")
        name.set("Jendrik Johannes")
        email.set("jendrik@gradlex.org")
    }
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 4
    inputs.dir(layout.projectDirectory.dir("samples"))
}
