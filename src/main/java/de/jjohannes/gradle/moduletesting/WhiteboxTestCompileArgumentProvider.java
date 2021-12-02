package de.jjohannes.gradle.moduletesting;


import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.jvm.JavaModuleDetector;
import org.gradle.process.CommandLineArgumentProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WhiteboxTestCompileArgumentProvider implements CommandLineArgumentProvider {
    private final Set<File> mainSourceFolders;
    private final Set<File> testSourceFolders;
    private final JavaCompile task;
    private final String testFrameworkAPI;
    private final List<String> testRequires;
    private final JavaModuleDetector moduleDetector;
    private final ModuleInfoParser moduleInfoParser;

    public WhiteboxTestCompileArgumentProvider(Set<File> mainSourceFolders, Set<File> testSourceFolders, JavaCompile task, String testFrameworkAPI, List<String> testRequires, JavaModuleDetector moduleDetector, ModuleInfoParser moduleInfoParser) {
        this.mainSourceFolders = mainSourceFolders;
        this.testSourceFolders = testSourceFolders;
        this.task = task;
        this.testFrameworkAPI = testFrameworkAPI;
        this.testRequires = testRequires;
        this.moduleDetector = moduleDetector;
        this.moduleInfoParser = moduleInfoParser;
    }

    @Override
    public Iterable<String> asArguments() {
        String moduleName = moduleInfoParser.moduleName(mainSourceFolders);
        String testSources = testSourceFolders.iterator().next().getPath();

        String cpSeparator = System.getProperty("path.separator");
        List<String> args = new ArrayList<>();

        // Since for Gradle this sources set does not look like a module, we have to define the module path ourselves
        args.add("--module-path");
        args.add(moduleDetector.inferModulePath(true, task.getClasspath()).getFiles().stream().map(File::getPath).collect(Collectors.joining(cpSeparator)));

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
        args.add(moduleName + "=" + testSources);

        return args;
    }
}
