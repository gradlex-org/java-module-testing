package de.jjohannes.gradle.moduletesting;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.internal.jvm.JavaModuleDetector;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public abstract class JavaModuleTestingExtension {
    private static final Action<WhiteboxTestSet> NO_OP_ACTION = c -> {};

    private final Project project;
    private final JavaModuleDetector moduleDetector;

    @Inject
    public JavaModuleTestingExtension(Project project, JavaModuleDetector moduleDetector) {
        this.project = project;
        this.moduleDetector = moduleDetector;
    }

    public void blackboxTests(String testSourceSetName) {
        SourceSet sourceSet = configureSourceSetForTesting(testSourceSetName);
        configureSourceSetForBlackbox(sourceSet);
    }

    public void junit5WhiteboxTests(String testSourceSetName, String junit5Version) {
        junit5WhiteboxTests(testSourceSetName, junit5Version, NO_OP_ACTION);
    }

    public void junit5WhiteboxTests(String testSourceSetName, String junit5Version, Action<WhiteboxTestSet> conf) {
        whiteboxTests(testSourceSetName, "org.junit.jupiter.api", "org.junit.jupiter:junit-jupiter-api:" + junit5Version, conf);
        project.getTasks().named(testSourceSetName, Test.class).configure(Test::useJUnitPlatform);
    }

    public void junit4WhiteboxTests(String testSourceSetName, String junit4Version) {
        junit4WhiteboxTests(testSourceSetName, junit4Version, NO_OP_ACTION);
    }

    public void junit4WhiteboxTests(String testSourceSetName, String junit4Version, Action<WhiteboxTestSet> conf) {
        whiteboxTests(testSourceSetName, "junit", "junit:junit:" + junit4Version, conf);
        project.getTasks().named(testSourceSetName, Test.class).configure(Test::useJUnit);
    }

    private void whiteboxTests(String testSourceSetName, String testFrameworkModuleName, String testFrameworkGAV, Action<WhiteboxTestSet> conf) {
        WhiteboxTestSet whiteboxTestSet = new WhiteboxTestSet();
        conf.execute(whiteboxTestSet);
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        SourceSet testSourceSet = configureSourceSetForTesting(testSourceSetName);
        configureSourceSetForWhitebox(mainSourceSet, testSourceSet, testFrameworkModuleName, testFrameworkGAV, whiteboxTestSet.getTestRequires());
    }

    private SourceSet configureSourceSetForTesting(String name) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        TaskContainer tasks = project.getTasks();
        SourceSet sourceSet = sourceSets.maybeCreate(name);

        TaskProvider<Test> testTask;
        if (!tasks.getNames().contains(name)) {
            testTask = tasks.register(name, Test.class, t -> {
                t.setDescription("Runs " + name + " tests.");
                t.setGroup("verification");
            });
            tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, t-> {
                t.dependsOn(testTask);
            });
        }

        return sourceSet;
    }

    private void configureSourceSetForBlackbox(SourceSet sourceSet) {
        ConfigurationContainer configurations = project.getConfigurations();
        DependencyHandler dependencies = project.getDependencies();
        TaskContainer tasks = project.getTasks();

        TaskProvider<Jar> jarTask;
        if (!tasks.getNames().contains(sourceSet.getJarTaskName())) {
            jarTask = tasks.register(sourceSet.getJarTaskName(), Jar.class, t -> {
                t.getArchiveClassifier().set(sourceSet.getName());
                t.from(sourceSet.getOutput());
            });
        } else {
            jarTask = tasks.named(sourceSet.getJarTaskName(), Jar.class);
        }

        tasks.named(sourceSet.getName(), Test.class, t -> {
            t.setClasspath(configurations.getByName(sourceSet.getRuntimeClasspathConfigurationName()).plus(project.files(jarTask)));
            t.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
        });
        dependencies.add(sourceSet.getImplementationConfigurationName(), project);
    }

    private void configureSourceSetForWhitebox(SourceSet mainSourceSet, SourceSet testSourceSet, String testFrameworkModuleName, String testFrameworkGAV, List<String> testRequires) {
        ConfigurationContainer configurations = project.getConfigurations();
        DependencyHandler dependencies = project.getDependencies();
        TaskContainer tasks = project.getTasks();
        ModuleInfoParser moduleInfoParser = new ModuleInfoParser(project.getLayout(), project.getProviders());

        tasks.named(testSourceSet.getCompileJavaTaskName(), JavaCompile.class, t -> {
            t.setClasspath(mainSourceSet.getOutput().plus(configurations.getByName(testSourceSet.getCompileClasspathConfigurationName())));
            t.getOptions().getCompilerArgumentProviders().add(new WhiteboxTestCompileArgumentProvider(
                    mainSourceSet.getJava().getSrcDirs(),
                    testSourceSet.getJava().getSrcDirs(),
                    t,
                    testFrameworkModuleName,
                    testRequires,
                    moduleDetector,
                    moduleInfoParser));
        });

        tasks.named(testSourceSet.getName(), Test.class, t -> {
            t.setClasspath(configurations.getByName(testSourceSet.getRuntimeClasspathConfigurationName()).plus(mainSourceSet.getOutput()).plus(testSourceSet.getOutput()));

            // Add main classes here so that Gradle finds module-info.class and treats this as a test with module path
            t.setTestClassesDirs(mainSourceSet.getOutput().getClassesDirs().plus(testSourceSet.getOutput().getClassesDirs()));

            t.getJvmArgumentProviders().add(new WhiteboxTestRuntimeArgumentProvider(
                    mainSourceSet.getJava().getSrcDirs(),
                    testSourceSet.getJava().getClassesDirectory(),
                    testFrameworkModuleName,
                    testRequires,
                    moduleInfoParser
            ));
        });

        Configuration implementation = configurations.getByName(testSourceSet.getImplementationConfigurationName());
        Configuration runtimeOnly = configurations.getByName(testSourceSet.getRuntimeOnlyConfigurationName());

        implementation.extendsFrom(configurations.getByName(mainSourceSet.getImplementationConfigurationName()));
        runtimeOnly.extendsFrom(configurations.getByName(mainSourceSet.getRuntimeOnlyConfigurationName()));

        dependencies.add(implementation.getName(), testFrameworkGAV);

        implementation.withDependencies(d -> {
            for (String requiresModuleName : testRequires) {
                Map<?, ?> gav = JavaModuleDependenciesBridge.gav(project, requiresModuleName);
                if (gav != null) {
                    dependencies.add(implementation.getName(), gav);
                }
            }
        });
    }

}
