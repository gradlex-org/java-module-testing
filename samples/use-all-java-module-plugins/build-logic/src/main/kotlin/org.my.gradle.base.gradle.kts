plugins {
    id("org.gradlex.extra-java-module-info")
    id("org.gradlex.java-module-dependencies")
}

group = "org.my"

extraJavaModuleInfo {
    automaticModule("org.apache.commons:commons-math3", "commons.math3")
}
