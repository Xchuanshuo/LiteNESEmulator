package com.legend.apu;

import com.legend.cpu.ICPU;
import com.legend.utils.ByteUtils;

/**
 * @author Legend
 * @data by on 20-5-2.
 * @description 噪音生成器
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Noise
 */
public class Noise implements SoundGenerator, DividerListener {

    private static final int[] PERIOD_TABLE = {4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068};

    private Envelope envelope = new Envelope();
    private Divider timer = new DecrementDivider();
    private LengthCounter lengthCounter = new LengthCounter();
    private boolean mode;
    private int period;
    private int feedbackRegister = 1;
    private boolean evenCycle = false;

    public Noise() {
        timer.setOutputClock(this);
    }

    @Override
    public void onClock(Divider divider) {
        int feedback = feedbackRegister;
        if (mode) {
            feedback ^= feedbackRegister >> 6;
        } else {
            feedback ^= feedbackRegister >> 1;
        }
        feedbackRegister >>= 1;
        feedbackRegister |= (feedback & 1) << 14;
    }

    @Override
    public void cycle(ICPU cpu) {
        evenCycle = !evenCycle;
        if (!evenCycle) return;
        timer.clock();
    }

    @Override
    public void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    @Override
    public void writeRegister(int index, int value) {
        switch (index) {
            case 0: // 0x400C
                envelope.setRegister(value);
                lengthCounter.setHalt(envelope.getLoopFlag());
                break;
            case 2: // 0x400E
                mode = ByteUtils.getBit(value, 7) != 0;
                int idx = ByteUtils.getBitsByRange(value, 0, 3);
                period = PERIOD_TABLE[idx];
                timer.setPeriod(period);
                break;
            case 3: // 0x400F
                lengthCounter.writeRegister(value);
                envelope.setStartFlag();
                break;
        }
    }

    @Override
    public int output() {
        if ((feedbackRegister & 1) != 0
                || lengthCounter.getLengthCounter() == 0) {
            return 0;
        } else {
            return envelope.output();
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
        envelope.clock();
    }
}
