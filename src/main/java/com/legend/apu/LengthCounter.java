package com.legend.apu;

import com.legend.utils.ByteUtils;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-5-2.
 * @description 长度计数器单元 用来自动控制波形通道的持续的时间 为0则设置为静音
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Length_Counter
 */
public class LengthCounter implements Serializable {

    private static final int[] LENGTH_COUNTER_TABLE = {
            10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30
    };

    private boolean isHalt;
    private int lengthCounter;
    private boolean isEnabled;

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!isEnabled) {
            this.lengthCounter = 0;
        }
    }

    public void setHalt(boolean halt) {
        this.isHalt = halt;
    }

    public void writeRegister(int value) {
        if (isEnabled) {
            int index = ByteUtils.getBitsByRange(value, 3, 7);
            this.lengthCounter = LENGTH_COUNTER_TABLE[index];
        }
    }

    public void clock() {
        if (!isHalt && lengthCounter != 0) {
            lengthCounter--;
        }
    }

    public int getLengthCounter() {
        return lengthCounter;
    }
}
