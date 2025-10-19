// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.test.samples;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.Sample;
import org.gradle.exemplar.test.runner.SampleModifier;

public class PluginBuildLocationSampleModifier implements SampleModifier {
    @Override
    public Sample modify(Sample sampleIn) {
        Command cmd = sampleIn.getCommands().remove(0);
        File pluginProjectDir = new File(".");
        sampleIn.getCommands()
                .add(new Command(
                        new File(pluginProjectDir, "gradlew").getAbsolutePath(),
                        cmd.getExecutionSubdirectory(),
                        Stream.concat(
                                        cmd.getArgs().stream(),
                                        Stream.of(
                                                "build",
                                                "--warning-mode=all",
                                                "-PpluginLocation=" + pluginProjectDir.getAbsolutePath()))
                                .collect(Collectors.toList()),
                        cmd.getFlags(),
                        cmd.getExpectedOutput(),
                        cmd.isExpectFailure(),
                        true,
                        cmd.isAllowDisorderedOutput(),
                        cmd.getUserInputs()));
        return sampleIn;
    }
}
