package com.legend.memory;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 混合(读写分别操作不同的地址)内存
 */
public class MixedMemory implements IMemory {

    private final IMemory readMemory, writeMemory;
    private final int readOffset, writeOffset;
    private final int size;

    public MixedMemory(IMemory readMemory, int readOffset, IMemory writeMemory,
                       int writeOffset, int size) {
        this.readMemory = readMemory;
        this.writeMemory = writeMemory;
        this.readOffset = readOffset;
        this.writeOffset = writeOffset;
        this.size = size;
    }

    @Override
    public int readByte(int address) {
        return readMemory.readByte(address + readOffset);
    }

    @Override
    public void writeByte(int address, int value) {
        writeMemory.writeByte(address + writeOffset, value);
    }

    @Override
    public int getSize() {
        return size;
    }
}
