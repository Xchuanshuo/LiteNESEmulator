package com.legend.utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Legend
 * @data by on 20-8-8.
 * @description id工具类
 */
public class IdUtils {

    private static Set<Integer> set = new HashSet<>();

    public static int getRandomId() {
        Random random = new Random();
        int id;
        do {
            id = random.nextInt(1000000);
        } while (set.contains(id));
        set.add(id);
        return id;
    }
}
