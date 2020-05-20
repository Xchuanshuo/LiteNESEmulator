package com.legend.memory;


/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 基于byte数组的内存实现
 */
public class DefaultMemory implements IMemory {

    protected final byte[] data;
    protected final int offset;
    protected final int size;

    public DefaultMemory(int size) {
        this(new byte[size]);
    }

    public DefaultMemory(byte[] data) {
        this(data, 0, data.length);
    }

    public DefaultMemory(byte[] data, int offset, int size) {
        this.data = data;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public int readByte(int address) {
        return data[(address + offset) % data.length] & 0xFF;
    }

    @Override
    public void writeByte(int address, int value) {
        data[address + offset] = (byte) value;
    }

    @Override
    public int getSize() {
        return size;
    }

}
