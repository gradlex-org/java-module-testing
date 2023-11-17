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

package org.gradlex.javamodule.testing.internal.provider;

import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradlex.javamodule.testing.internal.ModuleInfoParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class WhiteboxTestRuntimeArgumentProvider implements CommandLineArgumentProvider {
    private final Set<File> mainSourceFolders;
    private final Provider<Directory> testClassesFolders;
    private final File resourcesUnderTest;
    private final File testResources;
    private final ModuleInfoParser moduleInfoParser;

    private final ListProperty<String> allTestRequires;
    private final ListProperty<String> allTestOpensTo;

    public WhiteboxTestRuntimeArgumentProvider(Set<File> mainSourceFolders,
                                               Provider<Directory> testClassesFolders, File resourcesUnderTest, File testResources,
                                               ModuleInfoParser moduleInfoParser, ObjectFactory objects) {

        this.mainSourceFolders = mainSourceFolders;
        this.testClassesFolders = testClassesFolders;
        this.resourcesUnderTest = resourcesUnderTest;
        this.testResources = testResources;
        this.moduleInfoParser = moduleInfoParser;
        this.allTestRequires = objects.listProperty(String.class);
        this.allTestOpensTo = objects.listProperty(String.class);
    }

    public void testRequires(Provider<List<String>> testRequires) {
        allTestRequires.addAll(testRequires);
    }

    public void testRequires(List<String> testRequires) {
        allTestRequires.addAll(testRequires);
    }

    public void testOpensTo(Provider<List<String>> testRequires) {
        allTestOpensTo.addAll(testRequires);
    }

    public void testOpensTo(List<String> testRequires) {
        allTestOpensTo.addAll(testRequires);
    }

    @Override
    public Iterable<String> asArguments() {
        String moduleName = moduleInfoParser.moduleName(mainSourceFolders);

        Set<String> allTestClassPackages = new TreeSet<>();
        testClassesFolders.get().getAsFileTree().visit(file -> {
            String path = file.getPath();
            if (path.endsWith(".class") && path.contains("/")) {
                allTestClassPackages.add(path.substring(0, path.lastIndexOf("/")).replace('/', '.'));
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

        // Patch into Module located in the 'main' classes folder: test classes, resources, test resources
        args.add("--patch-module");
        args.add(moduleName + "=" +
                testClassesFolders.get().getAsFile().getPath() + File.pathSeparator +
                resourcesUnderTest.getPath() + File.pathSeparator +
                testResources.getPath());

        return args;
    }
}
