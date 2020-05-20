package com.legend.mapper;

import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.input.Input;
import com.legend.memory.*;
import com.legend.ppu.IPPU;
import com.legend.utils.ByteUtils;

import static com.legend.cartridges.INesLoader.HORIZONTAL;
import static com.legend.cartridges.INesLoader.ONE_SCREEN_MIRRORING;
import static com.legend.cartridges.INesLoader.VERTICAL;

/**
 * @author Legend
 * @data by on 20-5-7.
 * @description Mapper id = 1
 */
public class MMC1 extends Mapper implements IMemory {

    private IPPU ppu;
    private INesLoader loader;
    private StandardMemory mainMemory;
    private StandardMemory chrMemory = new StandardMemory(0x2000);

    private int chrBankIdx0, chrBankIdx1;
    private int prgRomBankMode;
    private int prgBankIdx;
    private boolean is8kbCHRBankMode = true;
    private boolean prgRamChipEnable;
    private IMemory fixedFirstBank, fixedLastBank;
    private int shifter = 0x10;
    private int control = 0xC0;

    @Override
    protected void mapMemoryImpl(StandardMemory memory, INesLoader loader, ICPU cpu, IPPU ppu, IAPU apu, Input input) {
        this.ppu = ppu;
        this.loader = loader;
        this.mainMemory = memory;
        if (loader.getCHRPageCount() == 0) {
            // 如 洛克人2 chrPage为0 所以无法从chrPage中获取
            chrMemory.setMemory(0, new DefaultMemory(0x2000));
        } else {
            chrMemory.setMemory(0, new DefaultMemory(loader.getCHRPageByIndex(0)));
        }
        ppu.setCHRMemory(chrMemory);

        this.fixedFirstBank = new ReadonlyMemory(loader.getPRGPageByIndex(0));
        this.fixedLastBank = new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1));
        mainMemory.setMemory(0x8000,  new MixedMemory(fixedFirstBank, 0,
                this, 0x8000, 0x4000));
        mainMemory.setMemory(0xC000, new MixedMemory(fixedLastBank, 0,
                this, 0xC000, 0x4000));
    }

    @Override
    public int readByte(int address) {
        return 0;
    }

    @Override
    public void writeByte(int address, int value) {
        // 模拟移位寄存器
        int d7 = ByteUtils.getBit(value, 7);
        if (d7 == 1) {
            shifter = 0x10;
            writeRegister(0x8000, control | 0xC);
        } else {
            boolean isFinished = (shifter & 1) == 1;
            shifter >>= 1;
            shifter |= ((value & 1) << 4);
            if (isFinished) {
                writeRegister(address, shifter);
                shifter = 0x10;
            }
        }

    }

    private void writeRegister(int address, int value) {
        switch (address & 0xE000) {
            case 0x8000: // Control (internal, $8000-$9FFF)
                this.control = value & 0x1F;
                int mirroring = ByteUtils.getBitsByRange(value, 0, 1);
                prgRomBankMode = ByteUtils.getBitsByRange(value, 2, 3);
                is8kbCHRBankMode = ByteUtils.getBit(value, 4) == 0;
                if (mirroring == 0) {
                    ppu.setMirroringType(ONE_SCREEN_MIRRORING, 0);
                } else if (mirroring == 1) {
                    ppu.setMirroringType(ONE_SCREEN_MIRRORING, 1);
                } else if (mirroring == 2) {
                    ppu.setMirroringType(VERTICAL);
                } else if (mirroring == 3) {
                    ppu.setMirroringType(HORIZONTAL);
                }
                break;
            case 0xA000: // CHR bank 0 (internal, $A000-$BFFF)
                this.chrBankIdx0 = ByteUtils.getBitsByRange(value, 0, 4) & 0x1F;
                switchCHRBank();
                break;
            case 0xC000: // CHR bank 1 (internal, $C000-$DFFF)
                if (is8kbCHRBankMode) return;
                this.chrBankIdx1 = value & 0x1F;
                switchCHRBank();
                break;
            case 0xE000: // PRG bank (internal, $E000-$FFFF)
                this.prgBankIdx = ByteUtils.getBitsByRange(value, 0, 3) & 0xF;
                prgRamChipEnable = ByteUtils.getBit(value, 4) == 0;
                switchPRGBank();
                break;
            default: break;
        }
    }

    private void switchCHRBank() {
        if (loader.getCHRPageCount() == 0) return;
        if (is8kbCHRBankMode) {
            int idx = chrBankIdx0 >> 1;
            IMemory bank1 = new DefaultMemory(loader.getCHRPageByIndex(idx), 0, 0x1000);
            IMemory bank2 = new DefaultMemory(loader.getCHRPageByIndex(idx), 0x1000, 0x1000);
            chrMemory.setMemory(0, bank1);
            chrMemory.setMemory(0x1000, bank2);
        } else {
            chrMemory.setMemory(0, new DefaultMemory(loader.getCHRPageByIndex(chrBankIdx0 / 2)
                    , (chrBankIdx0 & 1) * 0x1000, 0x1000));
            chrMemory.setMemory(0x1000, new DefaultMemory(loader.getCHRPageByIndex(chrBankIdx1 / 2)
                    , (chrBankIdx1 & 1) * 0x1000, 0x1000));
        }
    }

    private void switchPRGBank() {
        if (prgRomBankMode == 0 || prgRomBankMode == 1) {
            int idx = prgBankIdx;
            // 0, 1: switch 32 KB at $8000, ignoring low bit of bank number;
            IMemory bank1 = new ReadonlyMemory(loader.getPRGPageByIndex(idx));
            IMemory bank2 = new ReadonlyMemory(loader.getPRGPageByIndex(idx + 1));
            mainMemory.setMemory(0x8000, new MixedMemory(bank1, 0,
                    this, 0x8000, 0x4000));
            mainMemory.setMemory(0xC000, new MixedMemory(bank2, 0,
                    this, 0xC000, 0x4000));
        } else if (prgRomBankMode == 2) {
            // 2: fix first bank at $8000 and switch 16 KB bank at $C000;
            IMemory switchBank = new ReadonlyMemory(loader.getPRGPageByIndex(prgBankIdx));
            mainMemory.setMemory(0x8000, new MixedMemory(fixedFirstBank, 0,
                    this, 0x8000, 0x4000));
            mainMemory.setMemory(0xC000, new MixedMemory(switchBank, 0,
                    this, 0xC000, 0x4000));
        } else if (prgRomBankMode == 3) {
            // 3: fix last bank at $C000 and switch 16 KB bank at $8000)
            IMemory switchBank = new ReadonlyMemory(loader.getPRGPageByIndex(prgBankIdx));
            mainMemory.setMemory(0x8000, new MixedMemory(switchBank, 0,
                    this, 0x8000, 0x4000));
            mainMemory.setMemory(0xC000, new MixedMemory(fixedLastBank, 0,
                    this, 0xC000, 0x4000));
        }
    }

    @Override
    public int getSize() {
        return 0x8000;
    }
}
