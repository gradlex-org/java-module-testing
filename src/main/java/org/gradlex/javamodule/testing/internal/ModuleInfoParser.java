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

package org.gradlex.javamodule.testing.internal;

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

    static String moduleName(String moduleInfoFileContent) {
        boolean inComment = false;
        boolean moduleKeywordFound = false;

        for(String line: moduleInfoFileContent.split("\n")) {
            String cleanedLine = line
                    .replaceAll("/\\*.*\\*/", "") // open & close in this line
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
