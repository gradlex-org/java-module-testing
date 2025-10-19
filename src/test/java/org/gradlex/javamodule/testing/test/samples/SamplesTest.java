// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test.samples;

import org.gradle.exemplar.test.runner.SampleModifiers;
import org.gradle.exemplar.test.runner.SamplesRoot;
import org.gradle.exemplar.test.runner.SamplesRunner;
import org.gradlex.testing.NoCrossVersion;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(SamplesRunner.class)
@SamplesRoot("samples")
@SampleModifiers(PluginBuildLocationSampleModifier.class)
@Category(NoCrossVersion.class)
public class SamplesTest {}
