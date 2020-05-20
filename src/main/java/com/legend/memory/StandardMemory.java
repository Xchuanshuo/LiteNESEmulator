package com.legend.memory;

import java.io.Serializable;
import java.util.*;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 内存对象管家
 */
public class StandardMemory implements IMemory {

    private final int size;
    private List<MemoryHelper> memories = new ArrayList<>();
    private int[] offsets;

    public StandardMemory(int size) {
        this.size = size;
    }

    @Override
    public int readByte(int address) {
        MemoryHelper helper = findByOffset(address);
        assert helper != null;
//        System.out.println(String.format("%04x", address) + "---" + String.format("%04x", helper.offset));
        return helper.memory.readByte(address - helper.offset);
    }

    @Override
    public void writeByte(int address, int value) {
        MemoryHelper helper = findByOffset(address);
        assert helper != null;
        helper.memory.writeByte(address - helper.offset, value);
    }

    public void setMemory(int offset, IMemory memory) {
        MemoryHelper find = null;
        for (MemoryHelper cur : memories) {
            if (cur.offset == offset) {
                find = cur;
                break;
            }
        }
        if (find != null) {
            find.memory = memory;
        } else {
            memories.add(new MemoryHelper(memory, offset));
            memories.sort(Comparator.comparingInt(o -> o.offset));
            this.offsets = new int[memories.size()];
            for (int i = 0;i < offsets.length;i++) {
                offsets[i] = memories.get(i).offset;
            }
        }
    }

    // 目的是找到小于某个内存地址中的最大地址
    public MemoryHelper findByOffset(int address) {
        int index = Arrays.binarySearch(offsets, address);
        if (index == -1) return memories.get(0);
        if (index < 0) index = -index - 2;
        return memories.get(index);
    }

    public IMemory getMemory(int address) {
        MemoryHelper helper = findByOffset(address);
        return helper.memory;
    }

    @Override
    public int getSize() {
        return size;
    }

    class MemoryHelper implements Serializable {
        private int offset;
        private IMemory memory;

        MemoryHelper(IMemory memory, int offset) {
            this.memory = memory;
            this.offset = offset;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int o : offsets) {
            sb.append(Integer.toHexString(o)).append(", ");
        }
        return "StandardMemory{" +
                "size=" + size +
                ", memories=" + memories +
                ", offsets=" + sb.toString() +
                '}';
    }
}
