package com.legend.utils;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * @author Legend
 * @data by on 20-6-5.
 * @description 全局属性配置工具类
 */
public class PropertiesUtils {

    private static Properties properties = new Properties();

    static {
        try {
            File file = new File(Constants.GLOBAL_CONFIG_FILE);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream fis = new FileInputStream(Constants.GLOBAL_CONFIG_FILE);
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void put(String key, String value) {
        try (FileOutputStream fos = new FileOutputStream(Constants.GLOBAL_CONFIG_FILE)) {
            properties.setProperty(key, value);
            properties.store(fos, null);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static void remove(String key) {
        try (FileOutputStream fos = new FileOutputStream(Constants.GLOBAL_CONFIG_FILE)) {
            properties.remove(key);
            properties.store(fos, null);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> getKeys() {
        return properties.stringPropertyNames();
    }
}
