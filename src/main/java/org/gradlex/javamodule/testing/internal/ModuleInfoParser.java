// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ModuleInfoParser {

    private final ProjectLayout layout;
    private final ProviderFactory providers;

    public ModuleInfoParser(ProjectLayout layout, ProviderFactory providers) {
        this.layout = layout;
        this.providers = providers;
    }

    @Nullable
    public String moduleName(Set<File> sourceFolders) {
        for (File folder : sourceFolders) {
            Provider<RegularFile> moduleInfoFile =
                    layout.file(providers.provider(() -> new File(folder, "module-info.java")));
            Provider<String> moduleInfoContent =
                    providers.fileContents(moduleInfoFile).getAsText();
            if (moduleInfoContent.isPresent()) {
                return moduleName(moduleInfoContent.get());
            }
        }
        return null;
    }

    @Nullable
    static String moduleName(String moduleInfoFileContent) {
        boolean inComment = false;
        boolean moduleKeywordFound = false;

        for (String line : moduleInfoFileContent.split("\n")) {
            String cleanedLine = line.replaceAll("/\\*.*\\*/", "") // open & close in this line
                    .replaceAll("//.*", ""); // line comment
            inComment = inComment || cleanedLine.contains("/*");
            cleanedLine = cleanedLine.replaceAll("/\\*.*", ""); // open in this line
            inComment = inComment && !line.contains("*/");
            cleanedLine = cleanedLine.replaceAll(".*\\*/", "").trim(); // closing part of comment

            if (inComment) {
                continue;
            }

            List<String> tokens = Arrays.asList(cleanedLine.split("\\s+"));
            if (moduleKeywordFound && !tokens.isEmpty()) {
                return tokens.get(0);
            }

            int moduleKeywordIndex = tokens.indexOf("module");
            if (moduleKeywordIndex == 0 || moduleKeywordIndex == 1) {
                if (tokens.size() > moduleKeywordIndex) {
                    return tokens.get(moduleKeywordIndex + 1);
                }
                moduleKeywordFound = true;
            }
        }
        return null;
    }
}
