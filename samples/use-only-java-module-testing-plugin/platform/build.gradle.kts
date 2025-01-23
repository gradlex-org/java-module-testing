plugins {
    id("org.my.gradle.platform")
}


dependencies {
    api(platform("com.fasterxml.jackson:jackson-bom:2.13.2"))
    api(platform("org.junit:junit-bom:5.7.2"))
}

dependencies.constraints {
    api("org.apache.xmlbeans:xmlbeans:5.0.1")
    api("org.slf4j:slf4j-api:2.0.16")
    api("org.slf4j:slf4j-simple:2.0.16")
}