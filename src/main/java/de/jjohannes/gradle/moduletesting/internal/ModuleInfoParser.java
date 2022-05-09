package de.jjohannes.gradle.moduletesting.internal;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ModuleInfoParser {

    private final ProjectLayout layout;
    private final ProviderFactory providers;

    public ModuleInfoParser(ProjectLayout layout, ProviderFactory providers) {
        this.layout = layout;
        this.providers = providers;
    }

    public String moduleName(Set<File> sourceFolders) {
        for (File folder : sourceFolders) {
            Provider<RegularFile> moduleInfoFile = layout.file(providers.provider(() -> new File(folder, "module-info.java")));
            Provider<String> moduleInfoContent = providers.fileContents(moduleInfoFile).getAsText();
            if (moduleInfoContent.isPresent()) {
                return moduleName(moduleInfoContent.get());
            }
        }
        return null;
    }

    private String moduleName(String moduleInfoFileContent) {
        for(String line: moduleInfoFileContent.split("\n")) {
            List<String> tokens = Arrays.asList(line.replace("{","").trim().split("\\s+"));
            if (!"//".equals(tokens.get(0)) && tokens.contains("module")) {
                return tokens.get(tokens.size() - 1);
            }
        }
        return null;
    }
}
