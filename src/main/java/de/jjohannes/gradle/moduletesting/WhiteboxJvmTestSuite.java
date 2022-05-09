package de.jjohannes.gradle.moduletesting;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

public interface WhiteboxJvmTestSuite {

    Property<SourceSet> getSourcesUnderTest();

    ListProperty<String> getRequires();

    ListProperty<String> getOpensTo();
}
