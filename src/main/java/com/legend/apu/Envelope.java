package com.legend.apu;

import com.legend.utils.ByteUtils;

/**
 * @author Legend
 * @data by on 20-5-1.
 * @description 包络发生器 随着时间改变声音参数(音量 音色)
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Envelope
 */
public class Envelope implements DividerListener {

    private Divider divider = new DecrementDivider();
    private int decayLevel;
    private int volume;
    private boolean isConstant;
    private boolean startFlag;
    private boolean loopFlag;

    public Envelope() {
        this.divider.setOutputClock(this);
    }

    @Override
    public void onClock(Divider divider) {
        if (decayLevel > 0) {
            decayLevel--;
        } else if (loopFlag) {
            decayLevel = 15;
        }
    }

    // 帧计数器时钟触发
    public void clock() {
        if (isStartFlagCleared()) {
            divider.clock();
        } else {
            clearStartFlag();
            this.decayLevel = 15;
            divider.reset();
        }
    }

    public int output() {
        return isConstant ? volume : decayLevel;
    }

    public void setRegister(int value) {
        isConstant = ByteUtils.getBit(value, 4) != 0;
        loopFlag = ByteUtils.getBit(value, 5) != 0;
        volume = ByteUtils.getBitsByRange(value, 0, 3);
        divider.setPeriod(volume);
    }

    private boolean isStartFlagCleared() {
        return !startFlag;
    }

    private void clearStartFlag() {
        this.startFlag = false;
    }

    public void setStartFlag() {
        this.startFlag = true;
    }

    public void setLoopFlag() {
        this.loopFlag = true;
    }

    public boolean getLoopFlag() {
        return loopFlag;
    }
}
