package com.legend.memory;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 只读内存(ROM)
 */
public class ReadonlyMemory extends DefaultMemory {

    public ReadonlyMemory(int size) {
        super(size);
    }

    public ReadonlyMemory(byte[] data) {
        super(data);
    }

    public ReadonlyMemory(byte[] data, int offset, int size) {
        super(data, offset, size);
    }

    @Override
    public void writeByte(int address, int value) {
//        throw new UnsupportedOperationException("ReadonlyMemory cannot writeByte.---" + String.format("0x%04x", address));
    }

}
