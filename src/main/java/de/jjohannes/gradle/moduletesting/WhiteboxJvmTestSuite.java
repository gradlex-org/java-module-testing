package de.jjohannes.gradle.moduletesting;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

public interface WhiteboxJvmTestSuite {

    /**
     * Configure which source set contains the 'sources under test' for
     * this Whitebox Test Suite - defaults to 'main'.
     */
    Property<SourceSet> getSourcesUnderTest();

    /**
     * Add additional 'requires' directives for the test code.
     * For example, 'requires.add("org.junit.jupiter.api")'.
     */
    ListProperty<String> getRequires();

    /**
     * Open all packages of this Whitebox Test Suite to a given Module
     * for reflection at runtime.
     * For example, 'opensTo.add("org.junit.platform.commons")'.
     */
    ListProperty<String> getOpensTo();
}
