// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradlex.javamodule.testing.internal.ModuleInfoParser;

public class WhiteboxTestCompileArgumentProvider implements CommandLineArgumentProvider {
    private final Set<File> mainSourceFolders;
    private final Set<File> testSourceFolders;
    private final ModuleInfoParser moduleInfoParser;

    private final ListProperty<String> allTestRequires;

    public WhiteboxTestCompileArgumentProvider(
            Set<File> mainSourceFolders,
            Set<File> testSourceFolders,
            ModuleInfoParser moduleInfoParser,
            ObjectFactory objects) {
        this.mainSourceFolders = mainSourceFolders;
        this.testSourceFolders = testSourceFolders;
        this.moduleInfoParser = moduleInfoParser;
        this.allTestRequires = objects.listProperty(String.class);
    }

    public void testRequires(Provider<List<String>> testRequires) {
        allTestRequires.addAll(testRequires);
    }

    public void testRequires(List<String> testRequires) {
        allTestRequires.addAll(testRequires);
    }

    @Override
    public Iterable<String> asArguments() {
        String moduleName = moduleInfoParser.moduleName(mainSourceFolders);
        String testSources =
                testSourceFolders.stream().map(File::getPath).collect(Collectors.joining(File.pathSeparator));

        List<String> args = new ArrayList<>();

        for (String testRequires : allTestRequires.get()) {
            args.add("--add-modules");
            args.add(testRequires);
            args.add("--add-reads");
            args.add(moduleName + "=" + testRequires);
        }

        // Patch 'main' and 'test' sources together
        args.add("--patch-module");
        args.add(moduleName + "=" + testSources);

        return args;
    }
}
