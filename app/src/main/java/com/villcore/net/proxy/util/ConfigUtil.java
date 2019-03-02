package com.villcore.net.proxy.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ConfigUtil {

    public static Properties loadConfig(String configPath) {
        Properties clientProp = new Properties();
        try {
            clientProp.load(new FileReader(configPath));
        } catch (IOException e) {
            throw new RuntimeException("Load config " + configPath + " error");
        }
        return clientProp;
    }


    public static int getInt(Properties prop, String key) {
        return Integer.valueOf(prop.getProperty(key));
    }

    public static String get(Properties prop, String key) {
        return prop.getProperty(key);
    }
}
