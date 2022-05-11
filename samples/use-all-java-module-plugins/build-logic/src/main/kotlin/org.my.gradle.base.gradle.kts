plugins {
    id("de.jjohannes.extra-java-module-info")
    id("de.jjohannes.java-module-dependencies")
}

group = "org.my"

extraJavaModuleInfo {
    automaticModule("org.apache.commons:commons-math3", "commons.math3")
}
