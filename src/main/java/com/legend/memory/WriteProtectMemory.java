package com.legend.memory;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 写保护内存
 */
public class WriteProtectMemory extends DefaultMemory {

    private boolean isEnableWrite = true;

    public WriteProtectMemory(int size) {
        super(size);
    }

    public WriteProtectMemory(byte[] data) {
        super(data);
    }

    public WriteProtectMemory(byte[] data, int offset, int size) {
        super(data, offset, size);
    }

    @Override
    public void writeByte(int address, int value) {
        if (isEnableWrite) {
            super.writeByte(address, value);
        }
    }

    public void setEnableWrite(boolean enableWrite) {
        isEnableWrite = enableWrite;
    }
}
