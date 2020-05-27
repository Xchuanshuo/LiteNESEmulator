package com.legend.apu;

import com.legend.memory.DefaultMemory;
import com.legend.memory.IMemory;

import java.util.Arrays;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description APU寄存器 映射到主内存
 */
public class APURegister extends DefaultMemory implements IMemory {

    // 2 CPU cycles = 1 APU cycles
    static final int FRAME_COUNTER_PERIOD = 7458;
    int frameCounterTimer;
    private SoundGenerator[] soundGenerators;

    public APURegister(SoundGenerator...soundGenerators) {
        super(0x20);
        this.soundGenerators = soundGenerators;
    }

    @Override
    public int readByte(int address) {
        int result = super.readByte(address);
        switch (address) {
            case 0x15:
                int active = 0;
                int i = 0;
                for (SoundGenerator generator : soundGenerators) {
                    if (generator.isActive()) {
                        active |= (1 << i);
                    }
                    if (generator instanceof DMC) {
                        result |= ((DMC)generator).getInterruptFlag() ? 0x80 : 0;
                    }
                    i++;
                }
                result |= active;
                clearFrameInterrupt();
                break;
        }
        return result;
    }

    @Override
    public void writeByte(int address, int value) {
        super.writeByte(address, value);
        switch (address) {
            case 0x15:
                for (SoundGenerator generator : soundGenerators) {
                    generator.setEnabled((value & 1) != 0);
                    value >>= 1;
                }
                break;
            case 0x17:
                frameCounterTimer = -3;
                break;
            default:
                if ((address >> 2) < soundGenerators.length) {
                    soundGenerators[address >> 2].writeRegister(address & 3, value);
                }
                break;
        }
    }

    public boolean isInterruptDisable() {
        return (data[0x17] & 0x40) != 0;
    }

    public int getStepMode() {
        return (data[0x17] & 0x80) != 0 ? 5 : 4;
    }

    public void setStatusFrameCounterInterrupt() {
        data[0x15] |= 0x40;
    }

    public void clearFrameInterrupt() {
        data[0x15] &= ~0x40;
    }

    public boolean getStatusFrameCounterInterrupt() {
        return (data[0x15] &= 0x40) != 0;
    }

    public void reset() {
        Arrays.fill(data, (byte) 0);
        frameCounterTimer = -1;
    }

    @Override
    public String toString() {
        return "APURegister{" +
                "frameCounterTimer=" + frameCounterTimer +
                ", soundGenerators=" + Arrays.toString(soundGenerators) +
                ", data=" + Arrays.toString(data) +
                ", offset=" + offset +
                ", size=" + size +
                '}';
    }
}
