plugins {
    id("com.gradle.develocity") version "3.19"
}

dependencyResolutionManagement {
    repositories.mavenCentral()
}

rootProject.name = "java-module-testing"

develocity {
    buildScan {
        val isCi = providers.environmentVariable("CI").getOrElse("false").toBoolean()
        if (isCi) {
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"
        } else {
            publishing.onlyIf { false }
        }
    }
}
