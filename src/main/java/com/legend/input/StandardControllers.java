package com.legend.input;

import java.util.Arrays;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description 标准控制器
 */
public class StandardControllers extends Input {

    public static final int P1_KEY_A = 0;
    public static final int P1_KEY_B = 1;
    public static final int P1_KEY_SELECT = 2;
    public static final int P1_KEY_START = 3;
    public static final int P1_KEY_UP = 4;
    public static final int P1_KEY_DOWN = 5;
    public static final int P1_KEY_LEFT = 6;
    public static final int P1_KEY_RIGHT = 7;

    public static final int P2_KEY_A = 8;
    public static final int P2_KEY_B = 9;
    public static final int P2_KEY_SELECT = 10;
    public static final int P2_KEY_START = 11;
    public static final int P2_KEY_UP = 12;
    public static final int P2_KEY_DOWN = 13;
    public static final int P2_KEY_LEFT = 14;
    public static final int P2_KEY_RIGHT = 15;


    private boolean strobe;

    private int[] outputStatus = new int[2];
    private int[] pressStatus = new int[2];

    @Override
    protected void writeRegister(int value) {
        strobe = (value & 1) != 0;
        Arrays.fill(outputStatus, 0);
    }

    @Override
    protected int get(int index) {
        // 因为游戏汇编程序是直接从0x4016/0x4017第0位依次获取到8个按键的状态 所以这里
        // 用额外的空间来保存每个按钮的状态 取出一个按钮状态后依次递增 最多也只能取到8位
        int result = (pressStatus[index] >> outputStatus[index]) & 1;
        if (!strobe) {
            outputStatus[index] = (outputStatus[index] + 1) & 0x7;
        }
        return result;
    }

    public void press(int controllerId, int key) {
        pressStatus[controllerId] |= (1 << key);
    }

    public void release(int controllerId, int key) {
        pressStatus[controllerId] &= ~(1 << key);
    }

    public boolean isStrobe() {
        return strobe;
    }
}
