package org.my.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.spi.LoggerFactoryBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) throws IOException {
        doWork();
    }

    public static boolean doWork() throws IOException {
        ObjectMapper om = new ObjectMapper();
        if (!om.canSerialize(LoggerFactoryBinder.class)) {
            throw new RuntimeException("Boom!");
        }
        System.out.println(App.class.getModule().getName());

        printData();

        return true;
    }

    protected static void printData() throws IOException {
        try (InputStream is = App.class.getResourceAsStream("AppData.txt")) {
            System.out.println(new BufferedReader(new InputStreamReader(is)).readLine());
        }
    }
}