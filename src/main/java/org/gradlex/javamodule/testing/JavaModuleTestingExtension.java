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

package org.gradlex.javamodule.testing;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.jvm.JavaModuleDetector;
import org.gradle.jvm.tasks.Jar;
import org.gradle.testing.base.TestSuite;
import org.gradle.testing.base.TestingExtension;
import org.gradlex.javamodule.testing.internal.ModuleInfoParser;
import org.gradlex.javamodule.testing.internal.ModuleInfoRequiresParser;
import org.gradlex.javamodule.testing.internal.bridges.JavaModuleDependenciesBridge;
import org.gradlex.javamodule.testing.internal.provider.WhiteboxTestCompileArgumentProvider;
import org.gradlex.javamodule.testing.internal.provider.WhiteboxTestRuntimeArgumentProvider;

import javax.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class JavaModuleTestingExtension {
    private static final Action<WhiteboxJvmTestSuite> NO_OP_ACTION = c -> {};

    private final Project project;
    private final JavaModuleDetector moduleDetector;

    @Inject
    public JavaModuleTestingExtension(Project project, JavaModuleDetector moduleDetector) {
        this.project = project;
        this.moduleDetector = moduleDetector;

        TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
        testing.getSuites().withType(JvmTestSuite.class).configureEach(jvmTestSuite -> {
            boolean isTestModule = jvmTestSuite.getSources().getJava().getSrcDirs().stream().anyMatch(src -> new File(src, "module-info.java").exists());
            if ("test".equals(jvmTestSuite.getName())) {
                jvmTestSuite.useJUnitJupiter(); // override old Gradle default to default to JUnit5 for all suites
            }
            if (isTestModule) {
                blackbox(jvmTestSuite);
            } else {
                whitebox(jvmTestSuite, conf -> conf.getOpensTo().add("org.junit.platform.commons"));
            }
        });
    }

    /**
     * Turn the given JVM Test Suite into a Blackbox Test Suite.
     * For example:
     * <p>
     * javaModuleTesting.blackbox(testing.suites["integtest"])
     *
     * @param jvmTestSuite the JVM Test Suite to configure
     */
    public void blackbox(TestSuite jvmTestSuite) {
        if (jvmTestSuite instanceof JvmTestSuite) {
            configureJvmTestSuiteForBlackbox((JvmTestSuite) jvmTestSuite);
        }
    }

    /**
     * Turn the given JVM Test Suite into a Whitebox Test Suite.
     * For example:
     * <p>
     * javaModuleTesting.whitebox(testing.suites["test"])
     *
     * @param jvmTestSuite the JVM Test Suite to configure
     */
    @SuppressWarnings("unused")
    public void whitebox(TestSuite jvmTestSuite) {
        whitebox(jvmTestSuite, NO_OP_ACTION);
    }

    /**
     * Turn the given JVM Test Suite into a Whitebox Test Suite.
     * If needed, configure additional 'requires' and open the
     * test packages for reflection.
     * <p>
     * For example, for JUnit 5, you need at least:
     * <p>
     * javaModuleTesting.whitebox(testing.suites["test"]) {
     *   requires.add("org.junit.jupiter.api")
     *   opensTo.add("org.junit.platform.commons")
     * }
     *
     * @param jvmTestSuite the JVM Test Suite to configure
     * @param conf configuration details for the whitebox test setup
     */
    public void whitebox(TestSuite jvmTestSuite, Action<WhiteboxJvmTestSuite> conf) {
        if (jvmTestSuite instanceof JvmTestSuite) {
            WhiteboxJvmTestSuite whiteboxJvmTestSuite = project.getObjects().newInstance(WhiteboxJvmTestSuite.class);
            whiteboxJvmTestSuite.getSourcesUnderTest().convention(project.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME));
            whiteboxJvmTestSuite.getRequires().addAll(requiresFromModuleInfo((JvmTestSuite) jvmTestSuite, whiteboxJvmTestSuite.getSourcesUnderTest()));
            conf.execute(whiteboxJvmTestSuite);
            configureJvmTestSuiteForWhitebox((JvmTestSuite) jvmTestSuite, whiteboxJvmTestSuite);
        }
    }

    private Provider<List<String>> requiresFromModuleInfo(JvmTestSuite jvmTestSuite, Provider<SourceSet> sourcesUnderTest) {
        RegularFile moduleInfoFile = project.getLayout().getProjectDirectory().file(whiteboxModuleInfo(jvmTestSuite).getAbsolutePath());
        Provider<String> moduleInfoContent = project.getProviders().fileContents(moduleInfoFile).getAsText();
        return moduleInfoContent.map(c -> {
            ModuleInfoParser moduleInfoParser = new ModuleInfoParser(project.getLayout(), project.getProviders());
            String mainModuleName = moduleInfoParser.moduleName(sourcesUnderTest.get().getAllJava().getSrcDirs());
            List<String> requires = ModuleInfoRequiresParser.parse(moduleInfoContent.get());
            if (requires.stream().anyMatch(r -> r.equals(mainModuleName))) {
                return requires.stream().filter(r -> !r.equals(mainModuleName)).collect(Collectors.toList());
            }
            return Collections.<String>emptyList();
        }).orElse(Collections.emptyList());
    }

    private File whiteboxModuleInfo(JvmTestSuite jvmTestSuite) {
        File sourceSetDir = jvmTestSuite.getSources().getJava().getSrcDirs().iterator().next().getParentFile();
        return new File(sourceSetDir, "java9/module-info.java");
    }

    private void configureJvmTestSuiteForBlackbox(JvmTestSuite jvmTestSuite) {
        ConfigurationContainer configurations = project.getConfigurations();
        TaskContainer tasks = project.getTasks();

        TaskProvider<Jar> jarTask;
        SourceSet sourceSet = jvmTestSuite.getSources();
        if (!tasks.getNames().contains(sourceSet.getJarTaskName())) {
            jarTask = tasks.register(sourceSet.getJarTaskName(), Jar.class, t -> {
                t.getArchiveClassifier().set(sourceSet.getName());
                t.from(sourceSet.getOutput());
            });
        } else {
            jarTask = tasks.named(sourceSet.getJarTaskName(), Jar.class);
        }

        tasks.named(sourceSet.getName(), Test.class, t -> {
            // Classpath consists only of Jars to include classes+resources in one place
            t.setClasspath(configurations.getByName(sourceSet.getRuntimeClasspathConfigurationName()).plus(project.files(jarTask)));
            // Reset test classes dir
            t.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
        });
    }

    private void configureJvmTestSuiteForWhitebox(JvmTestSuite jvmTestSuite, WhiteboxJvmTestSuite whiteboxJvmTestSuite) {
        ConfigurationContainer configurations = project.getConfigurations();
        DependencyHandler dependencies = project.getDependencies();
        TaskContainer tasks = project.getTasks();
        ModuleInfoParser moduleInfoParser = new ModuleInfoParser(project.getLayout(), project.getProviders());

        SourceSet testSources = jvmTestSuite.getSources();
        JavaModuleDependenciesBridge.addRequiresRuntimeSupport(project, whiteboxJvmTestSuite.getSourcesUnderTest().get(), jvmTestSuite.getSources());

        tasks.named(testSources.getCompileJavaTaskName(), JavaCompile.class, compileJava -> {
            SourceSet sourcesUnderTest = whiteboxJvmTestSuite.getSourcesUnderTest().get();

            compileJava.setClasspath(sourcesUnderTest.getOutput().plus(configurations.getByName(testSources.getCompileClasspathConfigurationName())));
            FileCollection syntheticModuleInfoFolders = JavaModuleDependenciesBridge.addRequiresRuntimeSupportOld(project, compileJava, sourcesUnderTest);

            WhiteboxTestCompileArgumentProvider argumentProvider = (WhiteboxTestCompileArgumentProvider) compileJava.getOptions().getCompilerArgumentProviders().stream()
                    .filter(p -> p instanceof WhiteboxTestCompileArgumentProvider).findFirst().orElseGet(() -> {
                        WhiteboxTestCompileArgumentProvider newProvider = new WhiteboxTestCompileArgumentProvider(
                                sourcesUnderTest.getJava().getSrcDirs(),
                                testSources.getJava().getSrcDirs(),
                                compileJava,
                                syntheticModuleInfoFolders,
                                moduleDetector,
                                moduleInfoParser,
                                project.getObjects());
                        compileJava.getOptions().getCompilerArgumentProviders().add(newProvider);
                        return newProvider;
                    });
            argumentProvider.testRequires(JavaModuleDependenciesBridge.getCompileClasspathModules(project, testSources));
            argumentProvider.testRequires(whiteboxJvmTestSuite.getRequires());
        });

        tasks.named(testSources.getName(), Test.class, test -> {
            SourceSet sourcesUnderTest = whiteboxJvmTestSuite.getSourcesUnderTest().get();
            test.setClasspath(configurations.getByName(testSources.getRuntimeClasspathConfigurationName()).plus(sourcesUnderTest.getOutput()).plus(testSources.getOutput()));

            // Add main classes here so that Gradle finds module-info.class and treats this as a test with module path
            test.setTestClassesDirs(sourcesUnderTest.getOutput().getClassesDirs().plus(testSources.getOutput().getClassesDirs()));

            WhiteboxTestRuntimeArgumentProvider argumentProvider = (WhiteboxTestRuntimeArgumentProvider) test.getJvmArgumentProviders().stream()
                    .filter(p -> p instanceof WhiteboxTestRuntimeArgumentProvider).findFirst().orElseGet(() -> {
                        WhiteboxTestRuntimeArgumentProvider newProvider = new WhiteboxTestRuntimeArgumentProvider(
                                sourcesUnderTest.getJava().getSrcDirs(),
                                testSources.getJava().getClassesDirectory(),
                                sourcesUnderTest.getOutput().getResourcesDir(),
                                testSources.getOutput().getResourcesDir(),
                                moduleInfoParser,
                                project.getObjects());
                        test.getJvmArgumentProviders().add(newProvider);
                        return newProvider;
                    });
            argumentProvider.testRequires(JavaModuleDependenciesBridge.getRuntimeClasspathModules(project, testSources));
            argumentProvider.testRequires(whiteboxJvmTestSuite.getRequires());
            argumentProvider.testOpensTo(whiteboxJvmTestSuite.getOpensTo());
        });

        Configuration implementation = configurations.getByName(testSources.getImplementationConfigurationName());
        implementation.withDependencies(d -> {
            for (String requiresModuleName : whiteboxJvmTestSuite.getRequires().get()) {
                Provider<?> gav = JavaModuleDependenciesBridge.gav(project, requiresModuleName);
                if (gav != null) {
                    dependencies.addProvider(implementation.getName(), gav);
                }
            }
        });
    }
}
