package com.legend.apu;

import com.legend.utils.ByteUtils;

/**
 * @author Legend
 * @data by on 20-5-2.
 * @description APU扫描单元 用来定期向上或向下调整脉冲通道的周期
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Sweep
 */
public class Sweep implements DividerListener {

    private Divider divider = new DecrementDivider();
    private SweepListener sweepListener;
    private boolean isEnable;
    private boolean isNegate;
    private boolean reloadFlag;
    private int shiftCount;
    private int period;

    public Sweep() {
        this.divider.setOutputClock(this);
    }

    @Override
    public void onClock(Divider divider) {
        if (isEnable && sweepListener != null) {
            sweepListener.onSweep();
        }
    }

    public void clock() {
        if (reloadFlag) {
            if (isEnable && shiftCount != 0) {
                divider.clock();
            }
            reloadFlag = false;
            divider.reset();
        } else {
            divider.clock();
        }
    }

    public void writeRegister(int value) {
        isEnable = ByteUtils.getBit(value, 7) != 0;
        period = ByteUtils.getBitsByRange(value, 4, 6);
        isNegate = ByteUtils.getBit(value, 3) != 0;
        shiftCount = ByteUtils.getBitsByRange(value, 0, 2);
        reloadFlag = true;
        divider.setPeriod(period);
    }

    public int calculateTargetPeriod(int currentPeriod, boolean useOnesComponent) {
        int changeAmount = currentPeriod >> shiftCount;
        if (isNegate) {
            if (useOnesComponent) {
                changeAmount = ~changeAmount;
            } else {
                changeAmount = -changeAmount;
            }
        }
        return currentPeriod + changeAmount;
    }

    public void setSweepListener(SweepListener sweepListener) {
        this.sweepListener = sweepListener;
    }

    public int getShiftCount() {
        return shiftCount;
    }
}
