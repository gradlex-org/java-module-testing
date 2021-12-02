package de.jjohannes.gradle.moduletesting;

import org.gradle.api.Project;

import java.lang.reflect.Method;
import java.util.Map;

public class JavaModuleDependenciesBridge {

    public static Map<?, ?> gav(Project project, String moduleName) {
        Object javaModuleDependencies = project.getExtensions().findByName("javaModuleDependencies");
        if (javaModuleDependencies == null) {
            return null;
        }
        try {
            Method gav = javaModuleDependencies.getClass().getMethod("gav", String.class);
            return (Map<?, ?>) gav.invoke(javaModuleDependencies, moduleName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
