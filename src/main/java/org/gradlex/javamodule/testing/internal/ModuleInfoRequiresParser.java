// SPDX-License-Identifier: Apache-2.0
package org.gradlex.javamodule.testing.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleInfoRequiresParser {
    private static final String RUNTIME_KEYWORD = "/*runtime*/";

    public static List<String> parse(String moduleInfoFileContent, boolean runtimeOnly) {
        List<String> requires = new ArrayList<>();
        boolean insideComment = false;
        for (String line : moduleInfoFileContent.split("\n")) {
            insideComment = parseLine(line, insideComment, requires, runtimeOnly);
        }
        return requires;
    }

    /**
     * @return true, if we are inside a multi-line comment after this line
     */
    private static boolean parseLine(
            String moduleLine, boolean insideComment, List<String> requires, boolean runtimeOnly) {
        if (insideComment) {
            return !moduleLine.contains("*/");
        }

        List<String> tokens = Arrays.asList(moduleLine
                .replace(";", "")
                .replace("{", "")
                .replace(RUNTIME_KEYWORD, "runtime")
                .replaceAll("/\\*.*?\\*/", " ")
                .trim()
                .split("\\s+"));
        int singleLineCommentStartIndex = tokens.indexOf("//");
        if (singleLineCommentStartIndex >= 0) {
            tokens = tokens.subList(0, singleLineCommentStartIndex);
        }

        if (tokens.size() > 1 && tokens.get(0).equals("requires")) {
            if (runtimeOnly) {
                if (tokens.size() > 2 && tokens.contains("runtime")) {
                    requires.add(tokens.get(2));
                }
            } else {
                if (tokens.size() > 3 && tokens.contains("static") && tokens.contains("transitive")) {
                    requires.add(tokens.get(3));
                } else if (tokens.size() > 2 && tokens.contains("transitive")) {
                    requires.add(tokens.get(2));
                } else if (tokens.size() > 2 && tokens.contains("static")) {
                    requires.add(tokens.get(2));
                } else if (!tokens.contains("runtime")) {
                    requires.add(tokens.get(1));
                }
            }
        }
        return moduleLine.lastIndexOf("/*") > moduleLine.lastIndexOf("*/");
    }
}
