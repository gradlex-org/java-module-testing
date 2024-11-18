/*
 * Copyright the GradleX team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradlex.javamodule.testing;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;

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
