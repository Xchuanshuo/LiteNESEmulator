package com.legend.mapper;

import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.input.Input;
import com.legend.memory.*;
import com.legend.ppu.IPPU;

/**
 * @author Legend
 * @data by on 20-5-4.
 * @description Mapper id = 3
 */
public class CNROM extends Mapper implements IMemory {

    private INesLoader loader;
    private IPPU ppu;

    @Override
    protected void mapMemoryImpl(StandardMemory memory, INesLoader loader, ICPU cpu, IPPU ppu, IAPU apu, Input input) {
        this.loader = loader;
        this.ppu = ppu;
        memory.setMemory(0x8000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(0)),
                0, this, 0, 0x4000));
        memory.setMemory(0xC000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount()-1)),
                0, this, 0, 0x4000));
        // On reset, the first 8 KB VROM bank is loaded into PPU $0000.
        ppu.setCHRMemory(new DefaultMemory(loader.getCHRPageByIndex(0)));
    }

    @Override
    public int readByte(int address) {
        return 0;
    }

    @Override
    public void writeByte(int address, int value) {
        ppu.setCHRMemory(new DefaultMemory(loader.getCHRPageByIndex(value & 0x3)));
    }

    @Override
    public int getSize() {
        return 0x8000;
    }
}
