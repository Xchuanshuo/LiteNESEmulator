package com.legend.speaker;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description 从APU信号输出波
 */
public interface Speaker extends Serializable {

    int getSampleRate();
    /**
     * @param level from 0 ~ 128
     */
    void set(int level);
    byte[] output();
    void reset();
}
