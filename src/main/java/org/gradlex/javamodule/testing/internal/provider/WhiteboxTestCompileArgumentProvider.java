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

import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradlex.javamodule.testing.internal.ModuleInfoParser;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.jvm.JavaModuleDetector;
import org.gradle.process.CommandLineArgumentProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WhiteboxTestCompileArgumentProvider implements CommandLineArgumentProvider {
    private final Set<File> mainSourceFolders;
    private final Set<File> testSourceFolders;
    private final ModuleInfoParser moduleInfoParser;

    private final ListProperty<String> allTestRequires;

    public WhiteboxTestCompileArgumentProvider(
            Set<File> mainSourceFolders, Set<File> testSourceFolders, ModuleInfoParser moduleInfoParser, ObjectFactory objects) {
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
        String testSources = testSourceFolders.iterator().next().getPath();

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
