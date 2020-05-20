package com.legend.mapper;

import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.input.Input;
import com.legend.memory.IMemory;
import com.legend.memory.MixedMemory;
import com.legend.memory.ReadonlyMemory;
import com.legend.memory.StandardMemory;
import com.legend.ppu.IPPU;

/**
 * @author Legend
 * @data by on 20-5-4.
 * @description Mapper id = 2
 */
public class UNROM extends Mapper implements IMemory {

    private INesLoader loader;
    private StandardMemory mainMemory;

    @Override
    protected void mapMemoryImpl(StandardMemory memory, INesLoader loader, ICPU cpu, IPPU ppu, IAPU apu, Input input) {
        this.loader = loader;
        this.mainMemory = memory;
        memory.setMemory(0x8000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(0))
                , 0, this, 0, 0x4000));
        memory.setMemory(0xC000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1))
                , 0,this, 0, 0x4000));
    }

    @Override
    public int readByte(int address) {
        throw new UnsupportedOperationException("Cannot getByte from mapper register");
    }

    @Override
    public void writeByte(int address, int value) {
        mainMemory.setMemory(0x8000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(value & 0xF))
                , 0, this, 0, 0x4000));
    }

    @Override
    public int getSize() {
        return 0x8000;
    }
}
