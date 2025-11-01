// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface WhiteboxJvmTestSuite {

    /**
     * Configure which source set contains the 'sources under test' for
     * this Whitebox Test Suite - defaults to 'main'.
     *
     * @return the source set under test
     */
    Property<SourceSet> getSourcesUnderTest();

    /**
     * Add additional 'requires' directives for the test code.
     * For example, 'requires.add("org.junit.jupiter.api")'.
     *
     * @return modifiable list of addition 'requires' (--add-reads)
     */
    ListProperty<String> getRequires();

    /**
     * Add a runtime-only dependency via Module Name when combined with
     * 'java-module-dependencies' plugin.
     *
     * @return modifiable list of addition 'runtimeOnly' dependencies
     */
    ListProperty<String> getRequiresRuntime();

    /**
     * Open all packages of this Whitebox Test Suite to a given Module
     * for reflection at runtime.
     * For example, 'opensTo.add("org.junit.platform.commons")'.
     *
     * @return modifiable list of addition '--add-opens'
     */
    ListProperty<String> getOpensTo();

    /**
     * Export all packages of this Whitebox Test Suite to a given Module
     * for access to public methods at runtime.
     *
     * @return modifiable list of addition '--add-exports'
     */
    ListProperty<String> getExportsTo();
}
