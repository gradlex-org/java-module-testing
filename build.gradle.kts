version = "1.7"

publishingConventions {
    pluginPortal("${project.group}.${project.name}") {
        implementationClass("org.gradlex.javamodule.testing.JavaModuleTestingPlugin")
        displayName("Java Module Testing Gradle Plugin")
        description("A plugin to test Java Modules (whitebox and blackbox) without the hassle.")
        tags("gradlex", "java", "modularity", "jigsaw", "jpms", "testing")
    }
    gitHub("https://github.com/gradlex-org/java-module-testing")
    developer {
        id.set("jjohannes")
        name.set("Jendrik Johannes")
        email.set("jendrik@gradlex.org")
    }
}

testingConventions { testGradleVersions("7.4", "7.6.5", "8.0.2", "8.14.2") }

// === the following custom configuration should be removed once tests are migrated to Java
apply(plugin = "groovy")

tasks.named<GroovyCompile>("compileTestGroovy") { targetCompatibility = "11" } // allow tests to run against 6.x

dependencies { testImplementation("org.spockframework:spock-core:2.3-groovy-4.0") } //
// ====================================================================================
