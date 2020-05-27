package com.legend.apu;

import com.legend.cpu.ICPU;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-5-2.
 * @description 声音生成器接口
 */
public interface SoundGenerator extends Serializable {

    void cycle(ICPU cpu);
    void setEnabled(boolean enabled);
    void writeRegister(int index, int value);
    int output();
    boolean isActive();
    void clockLengthCounterAndSweep();
    void clockEnvelopeAndLinearCounter();
}
