// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JvmTestSuitePlugin;
import org.gradle.util.GradleVersion;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@NullMarked
public abstract class JavaModuleTestingPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        if (GradleVersion.current().compareTo(GradleVersion.version("7.4")) < 0) {
            throw new RuntimeException("This plugin requires Gradle 7.4+");
        }
        project.getPlugins().withType(JvmTestSuitePlugin.class, p -> project.getExtensions()
                .create("javaModuleTesting", JavaModuleTestingExtension.class));
    }
}
