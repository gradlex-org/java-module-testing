package de.jjohannes.gradle.moduletesting.internal.provider;

import de.jjohannes.gradle.moduletesting.internal.ModuleInfoParser;
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
    private final File resourcesUnderTest;
    private final File testResources;
    private final Provider<List<String>> testRequires;
    private final Provider<List<String>> opensTo;
    private final ModuleInfoParser moduleInfoParser;

    public WhiteboxTestRuntimeArgumentProvider(Set<File> mainSourceFolders,
           Provider<Directory> testClassesFolders, File resourcesUnderTest, File testResources,
           Provider<List<String>> testRequires, Provider<List<String>> opensTo, ModuleInfoParser moduleInfoParser) {

        this.mainSourceFolders = mainSourceFolders;
        this.testClassesFolders = testClassesFolders;
        this.resourcesUnderTest = resourcesUnderTest;
        this.testResources = testResources;
        this.testRequires = testRequires;
        this.opensTo = opensTo;
        this.moduleInfoParser = moduleInfoParser;
    }

    @Override
    public Iterable<String> asArguments() {
        String moduleName = moduleInfoParser.moduleName(mainSourceFolders);

        List<String> allTestClassPackages = new ArrayList<>();
        testClassesFolders.get().getAsFileTree().visit(file -> {
            String path = file.getPath();
            if (path.endsWith(".class") && path.contains("/")) {
                allTestClassPackages.add(path.substring(0, path.lastIndexOf("/")).replace('/', '.'));
            }
        });

        List<String> args = new ArrayList<>();

        for (String testRequires : testRequires.get()) {
            args.add("--add-modules");
            args.add(testRequires);
            args.add("--add-reads");
            args.add(moduleName + "=" + testRequires);
        }

        for (String packageName : allTestClassPackages) {
            for (String opensTo : opensTo.get()) {
                args.add("--add-opens");
                args.add(moduleName + "/" + packageName + "=" + opensTo);
            }
        }

        // Patch into Module located in the 'main' classes folder: test classes, resources, test resources
        args.add("--patch-module");
        args.add(moduleName + "=" +
                testClassesFolders.get().getAsFile().getPath() + File.pathSeparator +
                resourcesUnderTest.getPath() + File.pathSeparator +
                testResources.getPath());

        return args;
    }
}
