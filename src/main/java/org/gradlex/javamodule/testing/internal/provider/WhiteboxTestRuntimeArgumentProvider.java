// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradlex.javamodule.testing.internal.ModuleInfoParser;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class WhiteboxTestRuntimeArgumentProvider implements CommandLineArgumentProvider {
    private final Provider<Directory> testClassesFolders;
    private final File testResources;
    private final ModuleInfoParser moduleInfoParser;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Set<File> mainSourceFolders;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private File resourcesUnderTest;

    private final ListProperty<String> allTestRequires;
    private final ListProperty<String> allTestOpensTo;
    private final ListProperty<String> allTestExportsTo;

    public WhiteboxTestRuntimeArgumentProvider(
            Provider<Directory> testClassesFolders,
            File testResources,
            ModuleInfoParser moduleInfoParser,
            ObjectFactory objects) {
        this.testClassesFolders = testClassesFolders;
        this.testResources = testResources;
        this.moduleInfoParser = moduleInfoParser;
        this.allTestRequires = objects.listProperty(String.class);
        this.allTestOpensTo = objects.listProperty(String.class);
        this.allTestExportsTo = objects.listProperty(String.class);
    }

    public void setMainSourceFolders(Set<File> mainSourceFolders) {
        this.mainSourceFolders = mainSourceFolders;
    }

    public void setResourcesUnderTest(File resourcesUnderTest) {
        this.resourcesUnderTest = resourcesUnderTest;
    }

    public void testRequires(Provider<List<String>> testRequires) {
        allTestRequires.addAll(testRequires);
    }

    public void testRequires(List<String> testRequires) {
        allTestRequires.addAll(testRequires);
    }

    public void testOpensTo(Provider<List<String>> testOpensTo) {
        allTestOpensTo.addAll(testOpensTo);
    }

    public void testOpensTo(List<String> testOpensTo) {
        allTestOpensTo.addAll(testOpensTo);
    }

    public void testExportsTo(Provider<List<String>> testExportsTo) {
        allTestExportsTo.addAll(testExportsTo);
    }

    public void testExportsTo(List<String> testExportsTo) {
        allTestExportsTo.addAll(testExportsTo);
    }

    @Override
    public Iterable<String> asArguments() {
        String moduleName = moduleInfoParser.moduleName(mainSourceFolders);

        Set<String> allTestClassPackages = new TreeSet<>();
        testClassesFolders.get().getAsFileTree().visit(file -> {
            String path = file.getPath();
            if (path.endsWith(".class") && path.contains("/")) {
                allTestClassPackages.add(
                        path.substring(0, path.lastIndexOf("/")).replace('/', '.'));
            }
        });

        List<String> args = new ArrayList<>();

        for (String testRequires : allTestRequires.get()) {
            args.add("--add-modules");
            args.add(testRequires);
            args.add("--add-reads");
            args.add(moduleName + "=" + testRequires);
        }

        for (String packageName : allTestClassPackages) {
            for (String opensTo : allTestOpensTo.get()) {
                args.add("--add-opens");
                args.add(moduleName + "/" + packageName + "=" + opensTo);
            }
        }

        for (String packageName : allTestClassPackages) {
            for (String opensTo : allTestExportsTo.get()) {
                args.add("--add-exports");
                args.add(moduleName + "/" + packageName + "=" + opensTo);
            }
        }

        String testClassesPath = testClassesFolders.get().getAsFile().getPath();
        String resourcesUnderTestPath = toAppendablePathEntry(resourcesUnderTest);
        String testResourcesPath = toAppendablePathEntry(testResources);

        // Patch into Module located in the 'main' classes folder: test classes, resources, test resources
        args.add("--patch-module");
        args.add(moduleName + "=" + testClassesPath + resourcesUnderTestPath + testResourcesPath);

        return args;
    }

    private String toAppendablePathEntry(File folder) {
        if (folder.exists()) {
            return File.pathSeparator + folder.getPath();
        } else {
            return "";
        }
    }
}
