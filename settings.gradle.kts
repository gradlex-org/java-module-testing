plugins {
    id("com.gradle.enterprise") version "3.10"
}

dependencyResolutionManagement {
    repositories.mavenCentral()
}

rootProject.name = "java-module-testing"

gradleEnterprise {
    buildScan {
        publishAlways()
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
