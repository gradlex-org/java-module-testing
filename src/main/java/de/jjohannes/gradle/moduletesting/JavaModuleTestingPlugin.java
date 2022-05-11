package de.jjohannes.gradle.moduletesting;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

@SuppressWarnings("unused")
@NonNullApi
public abstract class JavaModuleTestingPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (GradleVersion.current().compareTo(GradleVersion.version("7.4")) < 0) {
            throw new RuntimeException("This plugin requires Gradle 7.4+");
        }

        project.getExtensions().create("javaModuleTesting", JavaModuleTestingExtension.class);
    }
}
