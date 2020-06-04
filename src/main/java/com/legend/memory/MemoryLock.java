package com.legend.memory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Legend
 * @data by on 20-6-4.
 * @description 内存锁定 用于作弊
 */
public class MemoryLock {

    private final static Set<Integer> mainMemoryLockSet = new HashSet<>();

    public static void lock(int address) {
        mainMemoryLockSet.add(address);
    }

    public static void unLock(int address) {
        mainMemoryLockSet.remove(address);
    }

    public static boolean isLock(int address) {
        return mainMemoryLockSet.contains(address);
    }

    public static void clearAll() {
        mainMemoryLockSet.clear();
    }
}
