package org.my.app;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppWhiteboxTest {

    @Test
    void printDataTest() throws IOException {
        App.printData();
        assertEquals("org.my.app", AppWhiteboxTest.class.getModule().getName());

        try (InputStream is = AppWhiteboxTest.class.getResourceAsStream("AppTestData.txt")) {
            System.out.println(new BufferedReader(new InputStreamReader(is)).readLine());
        }
    }

}