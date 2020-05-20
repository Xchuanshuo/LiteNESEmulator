package com.legend.input;

import com.legend.memory.IMemory;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description 游戏输入类 映射到主内存0x4016和0x4017
 */
public abstract class Input implements IMemory {
    protected abstract void writeRegister(int value);
    protected abstract int get(int address);

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public int readByte(int address) {
        return get(address);
    }

    @Override
    public void writeByte(int address, int value) {
        if (address == 0) {
            writeRegister(value);
        }
    }

//    public abstract int[] getPressStatus();
//    public abstract int[] getOutputStatus();
//    public abstract void setPressStatus(int[] status);
//    public abstract void setOutputStatus(int[] status);
//    public
}
