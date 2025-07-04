plugins {
    id("groovy")
    id("org.gradlex.internal.plugin-publish-conventions") version "0.6"
}

group = "org.gradlex"
version = "1.7"

tasks.withType<JavaCompile>().configureEach { options.release = 8 }

dependencies {
    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
    testImplementation("org.gradle.exemplar:samples-check:1.0.3")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

pluginPublishConventions {
    id("${project.group}.${project.name}")
    implementationClass("org.gradlex.javamodule.testing.JavaModuleTestingPlugin")
    displayName("Java Module Testing Gradle Plugin")
    description("A plugin to test Java Modules (whitebox and blackbox) without the hassle.")
    tags("gradlex", "java", "modularity", "jigsaw", "jpms", "testing")
    gitHub("https://github.com/gradlex-org/java-module-testing")
    developer {
        id.set("jjohannes")
        name.set("Jendrik Johannes")
        email.set("jendrik@gradlex.org")
    }
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    listOf("7.4", "7.6.5", "8.0.2", "8.14.2").forEach { gradleVersionUnderTest ->
        targets.register("test${gradleVersionUnderTest}") {
            testTask {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                description = "Runs tests against Gradle $gradleVersionUnderTest"
                systemProperty("gradleVersionUnderTest", gradleVersionUnderTest)
                exclude("**/*SamplesTest.class") // Not yet cross-version ready
            }
        }
    }
    targets.all {
        testTask {
            maxParallelForks = 4
            inputs.dir(layout.projectDirectory.dir("samples"))
            inputs.dir("samples")
        }
    }
}
