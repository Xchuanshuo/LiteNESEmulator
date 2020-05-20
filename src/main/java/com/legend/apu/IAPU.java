package com.legend.apu;

import com.legend.cpu.ICPU;
import com.legend.cpu.IRQGenerator;
import com.legend.speaker.Speaker;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description APU接口
 */
public interface IAPU extends IRQGenerator {

    APURegister getRegister();
    void setRegister(APURegister register);
    void writeRegister(int index, int value);
    int readRegister(int index);

    void cycle(Speaker speaker, ICPU cpu);
    void powerUp();
    void reset();
}
