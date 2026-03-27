// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal.actions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.internal.jvm.JavaModuleDetector;
import org.jspecify.annotations.NullMarked;

@NullMarked
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
        compilerArgs.add(infer("ModulePath", classpathAndModulePath).getAsPath());
        javaCompile.setClasspath(infer("Classpath", classpathAndModulePath));
        javaCompile.getOptions().setCompilerArgs(compilerArgs);
    }

    /**
     * Use reflective access for JavaModuleDetector methods so that it does not matter if JavaModuleDetector is a
     * class (<9.5.0) or an interface (9.5.0+).
     */
    private FileCollection infer(String pathType, FileCollection classpathAndModulePath) {
        try {
            Method inferModulePath =
                    JavaModuleDetector.class.getDeclaredMethod("infer" + pathType, boolean.class, FileCollection.class);
            return (FileCollection) inferModulePath.invoke(getJavaModuleDetector(), true, classpathAndModulePath);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
