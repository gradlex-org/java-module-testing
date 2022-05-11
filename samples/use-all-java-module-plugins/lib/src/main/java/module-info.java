module org.my.lib {
    requires transitive com.fasterxml.jackson.databind;

    // Patched to be a module
    requires commons.math3;

    // JDK modules
    requires java.logging;
    requires jdk.charsets;
}