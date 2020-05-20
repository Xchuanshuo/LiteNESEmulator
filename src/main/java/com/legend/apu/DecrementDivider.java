package com.legend.apu;

/**
 * @author Legend
 * @data by on 20-5-1.
 * @description 递减计数器(分频器)
 */
public class DecrementDivider implements Divider {

    private int period;
    private int counter;
    private DividerListener outputClock;

    @Override
    public void setPeriod(int value) {
        this.period = value;
    }

    @Override
    public void setOutputClock(DividerListener outputClock) {
        this.outputClock = outputClock;
    }

    @Override
    public void reset() {
        this.counter = period;
    }

    @Override
    public void clock() {
        if (counter == 0) {
            reset();
            triggerOutputClock();
        } else {
            counter--;
        }
    }

    private void triggerOutputClock() {
        if (outputClock != null) {
            outputClock.onClock(this);
        }
    }

    @Override
    public int getValue() {
        return counter;
    }
}
