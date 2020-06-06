package com.legend.common;

import com.legend.utils.PropertiesUtils;

import java.util.Set;

import static com.legend.utils.Constants.FIRST_RELOAD_FLAG;

/**
 * @author Legend
 * @data by on 20-6-6.
 * @description
 */
public class EmulatorShutdownHook extends Thread {

    @Override
    public void run() {
        Set<String> list = PropertiesUtils.getKeys();
        for (String key : list) {
            // 解决第一次Mapper163部分游戏第一次加载时闪屏问题
            if (key.startsWith(FIRST_RELOAD_FLAG)) {
                PropertiesUtils.remove(key);
            }
        }
    }
}
