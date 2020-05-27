package com.legend.apu;

import com.legend.cpu.ICPU;
import com.legend.utils.ByteUtils;

/**
 * @author Legend
 * @data by on 20-5-2.
 * @description 方波生成器
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Pulse
 */
public class Pulse implements SoundGenerator, DividerListener, SweepListener {

//    private static final int[][] DUTY_CYCLES = {
//            {0, 1, 0, 0, 0, 0, 0, 0},
//            {0, 1, 1, 0, 0, 0, 0, 0},
//            {0, 1, 1, 1, 1, 0, 0, 0},
//            {1, 0, 0, 1, 1, 1, 1, 1},
//    };

    private static final int[][] DUTY_CYCLES = {
            {0, 0, 0, 0, 0, 0, 0, 1},
            {0, 0, 0, 0, 0, 0, 1, 1},
            {0, 0, 0, 0, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 0},
    };

    private Envelope envelope = new Envelope();
    private Divider timer = new DecrementDivider();
    private Sequencer sequencer = new Sequencer(DUTY_CYCLES[0]);
    private Sweep sweep = new Sweep();
    private LengthCounter lengthCounter = new LengthCounter();

    private int currentPeriod;
    private int targetPeriod;
    private boolean evenCycle = false;
    private int id;

    public Pulse(int id) {
        this.id = id;
        this.timer.setOutputClock(this);
        this.sweep.setSweepListener(this);
    }

    @Override
    public void onClock(Divider divider) {
        sequencer.step();
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
            case 0: // 0x4000 | 0x4004
                envelope.setRegister(value);
                lengthCounter.setHalt(envelope.getLoopFlag());
                int idx = ByteUtils.getBitsByRange(value, 6, 7);
                sequencer.setSequence(DUTY_CYCLES[idx]);
                break;
            case 1: // 0x4001
                sweep.writeRegister(value);
                break;
            case 2: // 0x4005 | 0x4006
                currentPeriod = (currentPeriod & ~0xFF) | value;
                targetPeriod = sweep.calculateTargetPeriod(currentPeriod, id == 1);
                timer.setPeriod(currentPeriod);
                break;
            case 3: // 0x4003 | 4007
                // The sequencer is clocked by an 11-bit timer.
                currentPeriod = (currentPeriod & 0xFF) | ((value & 7) << 8);
                targetPeriod = sweep.calculateTargetPeriod(currentPeriod, id == 1);
                timer.setPeriod(currentPeriod);
                lengthCounter.writeRegister(value);
                sequencer.reset();
                envelope.setStartFlag();
                break;
        }
    }

    @Override
    public int output() {
        if (sequencer.output() == 0 || lengthCounter.getLengthCounter() == 0
                || targetPeriod > 0x7FF || timer.getValue() < 8) {
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
        sweep.clock();
    }

    @Override
    public void clockEnvelopeAndLinearCounter() {
        envelope.clock();
    }

    @Override
    public void onSweep() {
        // https://wiki.nesdev.com/w/index.php/APU_Sweep
        targetPeriod = sweep.calculateTargetPeriod(currentPeriod, id == 1);
        if (targetPeriod < 0) {
            targetPeriod = 0;
        }
        if (targetPeriod <= 0x7FF) {
            currentPeriod = targetPeriod;
        }
        if (sweep.getShiftCount() != 0) {
            timer.setPeriod(currentPeriod);
        }
    }
}
