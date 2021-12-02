package de.jjohannes.gradle.moduletesting;

import java.util.ArrayList;
import java.util.List;

public class WhiteboxTestSet {

    private final List<String> testRequires = new ArrayList<>();

    public void requires(String moduleName) {
        testRequires.add(moduleName);
    }

    public List<String> getTestRequires() {
        return testRequires;
    }
}
