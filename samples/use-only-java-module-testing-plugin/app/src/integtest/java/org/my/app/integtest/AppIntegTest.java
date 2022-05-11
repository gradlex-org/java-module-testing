package org.my.app.integtest;

import org.junit.jupiter.api.Test;
import org.my.app.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppIntegTest {

    @Test
    public void appDoesNotExplode() throws IOException {
        assertTrue(App.doWork());

        try (InputStream is = AppIntegTest.class.getResourceAsStream("AppTestData.txt")) {
            System.out.println(new BufferedReader(new InputStreamReader(is)).readLine());
        }
    }

}