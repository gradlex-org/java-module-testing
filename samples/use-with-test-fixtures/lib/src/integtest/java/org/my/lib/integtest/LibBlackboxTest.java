package org.my.lib.integtest;

import org.junit.jupiter.api.Test;
import org.my.lib.Lib;
import org.my.lib.test.fixtures.LibFixture;

public class LibBlackboxTest {

    @Test
    void testModule() {
        new LibFixture();
        new Lib().use();
        System.out.println("Integration Test Module: " + LibBlackboxTest.class.getModule().getName());
    }
}
