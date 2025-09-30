plugins {
    id("org.my.gradle.platform")
}


dependencies {
    api(platform("com.fasterxml.jackson:jackson-bom:2.20.0"))
    api(platform("org.junit:junit-bom:5.14.0"))
}

dependencies.constraints {
    api("org.apache.xmlbeans:xmlbeans:5.3.0")
    api("org.slf4j:slf4j-api:2.0.17")
    api("org.slf4j:slf4j-simple:2.0.17")
}