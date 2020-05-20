package com.legend.apu;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-5-1.
 * @description 分频器
 */
public interface Divider extends Serializable {
    void setPeriod(int value);
    void setOutputClock(DividerListener outputClock);
    void reset();
    void clock();
    int getValue();
}
