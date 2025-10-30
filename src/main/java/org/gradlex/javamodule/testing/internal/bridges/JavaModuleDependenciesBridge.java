// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal.bridges;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

public class JavaModuleDependenciesBridge {

    public static Provider<?> create(Project project, String moduleName, SourceSet sourceSetWithModuleInfo) {
        Object javaModuleDependencies = project.getExtensions().findByName("javaModuleDependencies");
        if (javaModuleDependencies == null) {
            return null;
        }
        try {
            Method gav = javaModuleDependencies.getClass().getMethod("create", String.class, SourceSet.class);
            return (Provider<?>) gav.invoke(javaModuleDependencies, moduleName, sourceSetWithModuleInfo);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addRequiresRuntimeSupport(
            Project project, SourceSet sourceSetForModuleInfo, SourceSet sourceSetForClasspath) {
        Object javaModuleDependencies = project.getExtensions().findByName("javaModuleDependencies");
        if (javaModuleDependencies == null) {
            return;
        }
        try {
            Method addRequiresRuntimeSupport = javaModuleDependencies
                    .getClass()
                    .getMethod("addRequiresRuntimeSupport", SourceSet.class, SourceSet.class);
            addRequiresRuntimeSupport.invoke(javaModuleDependencies, sourceSetForModuleInfo, sourceSetForClasspath);
        } catch (NoSuchMethodException e) {
            //noinspection UnnecessaryReturnStatement
            return;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getRuntimeClasspathModules(Project project, SourceSet sourceSet) {
        return getDeclaredModules("getRuntimeClasspathModules", project, sourceSet);
    }

    public static List<String> getCompileClasspathModules(Project project, SourceSet sourceSet) {
        return getDeclaredModules("getCompileClasspathModules", project, sourceSet);
    }

    public static List<String> getExportsToModules(Project project, SourceSet sourceSet) {
        return getDeclaredModules("getExportsToModules", project, sourceSet);
    }

    public static List<String> getOpensToModules(Project project, SourceSet sourceSet) {
        return getDeclaredModules("getOpensToModules", project, sourceSet);
    }

    private static List<String> getDeclaredModules(String getter, Project project, SourceSet sourceSet) {
        Object moduleInfoDslExtension = project.getExtensions().findByName(sourceSet.getName() + "ModuleInfo");
        if (moduleInfoDslExtension == null) {
            return Collections.emptyList();
        }
        try {
            Method gav = moduleInfoDslExtension.getClass().getMethod(getter);
            @SuppressWarnings("unchecked")
            List<String> modules = (List<String>) gav.invoke(moduleInfoDslExtension);
            return modules;
        } catch (NoSuchMethodException e) {
            return Collections.emptyList();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
