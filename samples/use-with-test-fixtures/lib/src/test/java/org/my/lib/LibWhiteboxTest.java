package org.my.lib;

import org.my.lib.test.fixtures.LibFixture;
import org.junit.jupiter.api.Test;

public class LibWhiteboxTest {

    @Test
    void testModule() {
        new LibFixture();
        new Lib().doStuff();
        System.out.println("Unit Test Module: " + LibWhiteboxTest.class.getModule().getName());
    }
}
