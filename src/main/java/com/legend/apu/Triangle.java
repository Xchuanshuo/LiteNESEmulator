package com.legend.apu;

import com.legend.cpu.ICPU;
import com.legend.utils.ByteUtils;

/**
 * @author Legend
 * @data by on 20-5-2.
 * @description 三角波生成器
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Triangle
 */
public class Triangle implements SoundGenerator, DividerListener {

    private static final int[] TRIANGLE_SEQUENCE = {
            15, 14, 13, 12, 11, 10,  9,  8,  7,  6,  5,  4,  3,  2,  1,  0,
            0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15
    };

    private Divider timer = new DecrementDivider();
    private LengthCounter lengthCounter = new LengthCounter();
    private Divider linearCounter = new DecrementDivider();
    private Sequencer sequencer = new Sequencer(TRIANGLE_SEQUENCE);
    private boolean reloadFlag;
    private boolean controlFlag;
    private int linearCounterReload;
    private int timerPeriod;

    public Triangle() {
        timer.setOutputClock(this);
    }

    @Override
    public void onClock(Divider divider) {
        if (linearCounter.getValue() != 0 && lengthCounter.getLengthCounter() != 0) {
            sequencer.step();
        }
    }

    @Override
    public void cycle(ICPU cpu) {
        timer.clock();
    }

    @Override
    public void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    @Override
    public void writeRegister(int index, int value) {
        switch (index) {
            case 0: // 0x4008
                controlFlag = ByteUtils.getBit(value, 7) != 0;
                linearCounterReload = ByteUtils.getBitsByRange(value, 0, 6);
                lengthCounter.setHalt(controlFlag);
                linearCounter.setPeriod(linearCounterReload);
                break;
            case 2: // 0x400A
                timerPeriod = (timerPeriod & ~0xFF) | value;
                timer.setPeriod(timerPeriod);
                break;
            case 3: // 0x400B
                timerPeriod = (timerPeriod & 0xFF) | ((value & 7) << 8);
                timer.setPeriod(timerPeriod);
                lengthCounter.writeRegister(value);
                reloadFlag = true;
                break;
        }
    }

    @Override
    public int output() {
        if (lengthCounter.getLengthCounter() == 0
                || linearCounter.getValue() == 0) {
            return 0;
        } else {
            return sequencer.output();
        }
    }

    @Override
    public boolean isActive() {
        return lengthCounter.getLengthCounter() > 0;
    }

    @Override
    public void clockLengthCounterAndSweep() {
        lengthCounter.clock();
    }

    @Override
    public void clockEnvelopeAndLinearCounter() {
        if (reloadFlag) {
            linearCounter.reset();
        } else {
            linearCounter.clock();
        }
        if (!controlFlag) {
            reloadFlag = false;
        }
    }
}
