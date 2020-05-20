package com.legend.mapper;

import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.input.Input;
import com.legend.memory.ReadonlyMemory;
import com.legend.memory.StandardMemory;
import com.legend.ppu.IPPU;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description Mapper id = 0
 */
public class NROM extends Mapper {

    @Override
    protected void mapMemoryImpl(StandardMemory memory, INesLoader loader, ICPU cpu, IPPU ppu, IAPU apu, Input input) {
        memory.setMemory(0x8000, new ReadonlyMemory(loader.getPRGPageByIndex(0)));
        memory.setMemory(0xC000, new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1)));
    }
}
