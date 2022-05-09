package de.jjohannes.gradle.moduletesting.internal.bridges;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import java.lang.reflect.Method;

public class JavaModuleDependenciesBridge {

    public static Provider<?> gav(Project project, String moduleName) {
        Object javaModuleDependencies = project.getExtensions().findByName("javaModuleDependencies");
        if (javaModuleDependencies == null) {
            return null;
        }
        try {
            Method gav = javaModuleDependencies.getClass().getMethod("gav", String.class);
            return (Provider<?>) gav.invoke(javaModuleDependencies, moduleName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
