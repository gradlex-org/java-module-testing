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

package org.gradlex.javamodule.testing.internal.actions;

import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.NonNullApi;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.jvm.JavaModuleDetector;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@NonNullApi
public abstract class JavaCompileSetModulePathAction implements Action<Task>, Describable {

    @Inject
    protected abstract JavaModuleDetector getJavaModuleDetector();

    @Override
    public String getDisplayName() {
        return JavaCompileSetModulePathAction.class.getName();
    }

    @Override
    public void execute(Task task) {
        JavaCompile javaCompile = (JavaCompile) task;
        FileCollection classpathAndModulePath = javaCompile.getClasspath();
        List<String> compilerArgs = new ArrayList<>(javaCompile.getOptions().getCompilerArgs());

        // Since for Gradle this sources set does not look like a module, we have to define the module path ourselves
        compilerArgs.add("--module-path");
        compilerArgs.add(getJavaModuleDetector().inferModulePath(true, classpathAndModulePath).getAsPath());
        javaCompile.setClasspath(getJavaModuleDetector().inferClasspath(true, classpathAndModulePath));
        javaCompile.getOptions().setCompilerArgs(compilerArgs);
    }
}
