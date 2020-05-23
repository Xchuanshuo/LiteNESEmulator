package com.legend.mapper;

import com.legend.apu.APURegister;
import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.cpu.IRQGenerator;
import com.legend.input.Input;
import com.legend.main.GameRunner;
import com.legend.memory.*;
import com.legend.ppu.IPPU;
import com.legend.ppu.SpriteDMARegister;
import com.legend.storage.ISave;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description
 */
public abstract class Mapper implements ISave, Serializable {

    private static final long serialVersionUID = 1186147812604977308L;

    protected StandardMemory initFirst4020BytesMemory(ICPU cpu, IPPU ppu, IAPU apu, Input input) {
        StandardMemory memory = new StandardMemory(0x10000);
        SpriteDMARegister dmaRegister = new SpriteDMARegister(memory, ppu.getSprRAM(), cpu);
        IMemory internalMemory = new DefaultMemory(0x800);
        memory.setMemory(0, internalMemory);
        memory.setMemory(0x800, new MirrorMemory(internalMemory, 0x1800));
        memory.setMemory(0x2000, ppu.getRegister());
        memory.setMemory(0x2008, new MirrorMemory(ppu.getRegister(), 0x1FF8));
        memory.setMemory(0x4000, new DefaultMemory(0x20));
        memory.setMemory(0x4014, dmaRegister);
        memory.setMemory(0x4015, new DefaultMemory(0x1));
        memory.setMemory(0x4016, input);
        memory.setMemory(0x4017, new DefaultMemory(0x1));
        memory.setMemory(0x4018, new DefaultMemory(0x1));
        if (apu != null) {
            APURegister apuRegister = apu.getRegister();
            memory.setMemory(0x4000, apuRegister);
            memory.setMemory(0x4015, new MirrorMemory(apuRegister, 0x15, 1));
            memory.setMemory(0x4017, new MixedMemory(input, 1, apuRegister, 0x17, 1));
            memory.setMemory(0x4018, new MirrorMemory(apuRegister, 0x18, 0x4020 - 0x4018));
        }
        return memory;
    }

    public void mapMemory(GameRunner runner) {
        INesLoader loader = runner.getLoader();
        ICPU cpu = runner.getCPU();
        IPPU ppu = runner.getPPU();
        IAPU apu = runner.getAPU();
        Input input = runner.getInput();
        if (loader.isFourScreenMirroring()) {
            ppu.setMirroringType(INesLoader.FOUR_SCREEN_MIRRORING);
        } else {
            ppu.setMirroringType(loader.getMirroringDirection());
        }
        StandardMemory mainMemory = initFirst4020BytesMemory(cpu, ppu, apu, input);
        if (loader.isSRAMEnable()) {
            mainMemory.setMemory(0x6000, new DefaultMemory(0x2000));
        } else if (loader.is512ByteTrainerPresent()) {
            mainMemory.setMemory(0x7000, new ReadonlyMemory(loader.getTrainer()));
        }
        if (loader.getCHRPageCount() != 0) {
            ppu.setCHRMemory(new DefaultMemory(loader.getCHRPageByIndex(0)));
        } else {
            ppu.setCHRMemory(new DefaultMemory(0x2000));
        }
        cpu.setMemory(mainMemory);

        mapMemoryImpl(runner);
        mapMemoryImpl(mainMemory, loader, cpu, ppu, apu, input);

        if (apu != null) {
            cpu.addIRQGenerator(apu);
        }
        if (this instanceof IRQGenerator) {
            cpu.addIRQGenerator((IRQGenerator) this);
        }
    }

    protected void mapMemoryImpl(GameRunner gameRunner) {
    }

    protected void mapMemoryImpl(StandardMemory memory, INesLoader loader, ICPU cpu, IPPU ppu, IAPU apu, Input input) {

    }

    @Override
    public byte[] getSaveBytes() throws IOException {
        return new byte[0];
    }

    @Override
    public void reload(byte[] bytes) throws IOException {}

    public void cycle(ICPU cpu) {}
}
