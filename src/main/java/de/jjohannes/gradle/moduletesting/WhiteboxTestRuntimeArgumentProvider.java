package de.jjohannes.gradle.moduletesting;

import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.process.CommandLineArgumentProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WhiteboxTestRuntimeArgumentProvider implements CommandLineArgumentProvider {
    private final Set<File> mainSourceFolders;
    private final Provider<Directory> testClassesFolders;
    private final String testFrameworkAPI;
    private final List<String> testRequires;
    private final ModuleInfoParser moduleInfoParser;

    public WhiteboxTestRuntimeArgumentProvider(Set<File> mainSourceFolders, Provider<Directory> testClassesFolders, String testFrameworkAPI, List<String> testRequires, ModuleInfoParser moduleInfoParser) {
        this.mainSourceFolders = mainSourceFolders;
        this.testClassesFolders = testClassesFolders;
        this.testFrameworkAPI = testFrameworkAPI;
        this.testRequires = testRequires;
        this.moduleInfoParser = moduleInfoParser;
    }

    @Override
    public Iterable<String> asArguments() {
        String moduleName = moduleInfoParser.moduleName(mainSourceFolders);
        String testClasses = testClassesFolders.get().getAsFile().getPath();

        List<String> args = new ArrayList<>();

        // Add module dependency to test framework
        args.add("--add-modules");
        args.add(testFrameworkAPI);
        args.add("--add-reads");
        args.add(moduleName + "=" + testFrameworkAPI);

        for (String testRequires : testRequires) {
            args.add("--add-modules");
            args.add(testRequires);
            args.add("--add-reads");
            args.add(moduleName + "=" + testRequires);
        }

        // Patch 'main' and 'test' sources together
        args.add("--patch-module");
        args.add(moduleName + "=" + testClasses);

        return args;
    }
}
