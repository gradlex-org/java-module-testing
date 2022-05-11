package de.jjohannes.gradle.moduletesting;

import de.jjohannes.gradle.moduletesting.internal.ModuleInfoParser;
import de.jjohannes.gradle.moduletesting.internal.bridges.JavaModuleDependenciesBridge;
import de.jjohannes.gradle.moduletesting.internal.provider.WhiteboxTestCompileArgumentProvider;
import de.jjohannes.gradle.moduletesting.internal.provider.WhiteboxTestRuntimeArgumentProvider;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
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

import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
public abstract class JavaModuleTestingExtension {
    private static final Action<WhiteboxJvmTestSuite> NO_OP_ACTION = c -> {};

    private final Project project;
    private final JavaModuleDetector moduleDetector;

    @Inject
    public JavaModuleTestingExtension(Project project, JavaModuleDetector moduleDetector) {
        this.project = project;
        this.moduleDetector = moduleDetector;
    }

    /**
     * Turn the given JVM Test Suite into a Blackbox Test Suite.
     * For example:
     *
     * javaModuleTesting.blackbox(testing.suites["integtest"])
     */
    public void blackbox(TestSuite jvmTestSuite) {
        if (jvmTestSuite instanceof JvmTestSuite) {
            configureJvmTestSuiteForBlackbox((JvmTestSuite) jvmTestSuite);
        }
    }

    /**
     * Turn the given JVM Test Suite into a Whitebox Test Suite.
     * For example:
     *
     * javaModuleTesting.whitebox(testing.suites["test"])
     */
    public void whitebox(TestSuite jvmTestSuite) {
        whitebox(jvmTestSuite, NO_OP_ACTION);

    }

    /**
     * Turn the given JVM Test Suite into a Whitebox Test Suite.
     * If needed, configure additional 'requires' and open the
     * test packages for reflection.
     *
     * For example, for JUnit 5, you need at least:
     *
     * javaModuleTesting.whitebox(testing.suites["test"]) {
     *   requires.add("org.junit.jupiter.api")
     *   opensTo.add("org.junit.platform.commons")
     * }
     */
    public void whitebox(TestSuite jvmTestSuite, Action<WhiteboxJvmTestSuite> conf) {
        if (jvmTestSuite instanceof JvmTestSuite) {
            WhiteboxJvmTestSuite whiteboxJvmTestSuite = project.getObjects().newInstance(WhiteboxJvmTestSuite.class);
            whiteboxJvmTestSuite.getSourcesUnderTest().convention(project.getExtensions().getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME));
            conf.execute(whiteboxJvmTestSuite);
            configureJvmTestSuiteForWhitebox((JvmTestSuite) jvmTestSuite, whiteboxJvmTestSuite);
        }
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
            // Rest test classes dir to allow switching back from 'whitebox' to 'blackbox'
            t.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
        });
    }

    private void configureJvmTestSuiteForWhitebox(JvmTestSuite jvmTestSuite, WhiteboxJvmTestSuite whiteboxJvmTestSuite) {
        ConfigurationContainer configurations = project.getConfigurations();
        DependencyHandler dependencies = project.getDependencies();
        TaskContainer tasks = project.getTasks();
        ModuleInfoParser moduleInfoParser = new ModuleInfoParser(project.getLayout(), project.getProviders());

        SourceSet testSources = jvmTestSuite.getSources();
        tasks.named(testSources.getCompileJavaTaskName(), JavaCompile.class, compileJava -> {
            SourceSet sourcesUnderTest = whiteboxJvmTestSuite.getSourcesUnderTest().get();
            compileJava.setClasspath(sourcesUnderTest.getOutput().plus(configurations.getByName(testSources.getCompileClasspathConfigurationName())));
            compileJava.getOptions().getCompilerArgumentProviders().add(new WhiteboxTestCompileArgumentProvider(
                    sourcesUnderTest.getJava().getSrcDirs(),
                    testSources.getJava().getSrcDirs(),
                    compileJava,
                    whiteboxJvmTestSuite.getRequires(),
                    moduleDetector,
                    moduleInfoParser));
        });

        tasks.named(testSources.getName(), Test.class, test -> {
            SourceSet sourcesUnderTest = whiteboxJvmTestSuite.getSourcesUnderTest().get();
            test.setClasspath(configurations.getByName(testSources.getRuntimeClasspathConfigurationName()).plus(sourcesUnderTest.getOutput()).plus(testSources.getOutput()));

            // Add main classes here so that Gradle finds module-info.class and treats this as a test with module path
            test.setTestClassesDirs(sourcesUnderTest.getOutput().getClassesDirs().plus(testSources.getOutput().getClassesDirs()));

            test.getJvmArgumentProviders().add(new WhiteboxTestRuntimeArgumentProvider(
                    sourcesUnderTest.getJava().getSrcDirs(),
                    testSources.getJava().getClassesDirectory(),
                    sourcesUnderTest.getOutput().getResourcesDir(),
                    testSources.getOutput().getResourcesDir(),
                    whiteboxJvmTestSuite.getRequires(),
                    whiteboxJvmTestSuite.getOpensTo(),
                    moduleInfoParser
            ));
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
