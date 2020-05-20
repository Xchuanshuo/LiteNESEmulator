package com.legend.memory;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 镜像内存
 */
public class MirrorMemory implements IMemory {

    private final IMemory source;
    private final int offset;
    private final int size;

    public MirrorMemory(IMemory source, int size) {
        this(source, 0, size);
    }

    public MirrorMemory(IMemory source) {
        this(source, 0, source.getSize());
    }

    public MirrorMemory(IMemory source, int offset, int size) {
        this.source = source;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public int readByte(int address) {
        return source.readByte((address + offset) % source.getSize());
    }

    @Override
    public void writeByte(int address, int value) {
        source.writeByte((address + offset) % source.getSize(), value);
    }

    @Override
    public int getSize() {
        return size;
    }
}
