package com.legend.mapper;

import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.cpu.IRQGenerator;
import com.legend.input.Input;
import com.legend.memory.*;
import com.legend.ppu.IPPU;
import com.legend.ppu.PPURegister;

/**
 * @author Legend
 * @data by on 20-5-5.
 * @description Mapper id = 4
 *  VROM = CHR-ROM
 *  idea:
 *      https://wiki.nesdev.com/w/index.php/MMC3
 */
public class MMC3 extends Mapper implements IMemory, IRQGenerator {

    private IPPU ppu;
    private INesLoader loader;
    private StandardMemory mainMemory;
    private int irqCounter;
    private int irqLatch;
    private boolean irqEnabled = false;
    private boolean generateIRQ = false;
    private IMemory prgSecondLastBank,prgLastBank;

    private StandardMemory chrMemory;
    private StandardMemory chr2KBBanks = new StandardMemory(0x1000);
    private StandardMemory chr1KBBanks = new StandardMemory(0x1000);
    private boolean prgBankMode = false;
    private boolean chrInversion = false;
    private int selectedR;

    private int[] r = new int[8];

    @Override
    protected void mapMemoryImpl(StandardMemory memory, INesLoader loader,
                                 ICPU cpu, IPPU ppu, IAPU apu, Input input) {
        this.mainMemory = memory;
        this.loader = loader;
        this.ppu = ppu;
        this.chrMemory = new StandardMemory(0x2000);
        ppu.setCHRMemory(chrMemory);

        prgLastBank = new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1)
                , 0x2000, 0x2000);
        this.prgSecondLastBank = new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1)
                , 0, 0x2000);
        chrMemory.setMemory(0, new DefaultMemory(loader.getCHRPageByIndex(0)));
        mainMemory.setMemory(0x8000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(0)),
                0, this, 0x8000, 0x2000));
        mainMemory.setMemory(0xC000, new MixedMemory(new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1)),
                0, this, 0x8000, 0x2000));
//        switchCHRBanks();
//        switchPRGBanks();
    }

    @Override
    public void cycle(ICPU cpu) {
        PPURegister r = ppu.getRegister();
        int cycle = ppu.getCycle();
        int scanline = ppu.getScanline();
        int cyclePos = r.getBackgroundPatternTableAddress() == 0 ? 260 : 324;
        if ((cycle >= cyclePos && cycle <= cyclePos + 2) && ppu.isVisibleRangeScanline()) {
            if (irqCounter == 0) {
                irqCounter = irqLatch;
            } else {
                irqCounter--;
                if (irqCounter == 0 && irqEnabled) {
                    generateIRQ = true;
                }
            }
        }
    }

    @Override
    public int readByte(int address) {
        return 0;
    }

    @Override
    public void writeByte(int address, int value) {
        switch (address & 0xE001) {
            case 0x8000: // $8000-$9FFE Bank select
                prgBankMode = (value & 0x40) != 0;
                chrInversion = (value & 0x80) != 0;
                selectedR = value & 7;
                break;
            case 0x8001: // $8001-$9FFF Bank data
                r[selectedR] = value;
                updateBanks(selectedR);
                break;
            case 0xA000: // $A000-$BFFE Mirroring
                if (!loader.isFourScreenMirroring()) {
                    if (loader.getFileMD5().equals("41DB95C3E6328DBE4624CE6E8341D7E8")) {
                        // 神龟pk无需切换
                        return;
                    }
                    if ((value & 1) == 1) {
                        ppu.setMirroringType(INesLoader.HORIZONTAL);
                    } else {
                        ppu.setMirroringType(INesLoader.VERTICAL);
                    }
                }
                break;
            case 0xA001: // $A001-$BFFF PRG RAM protect
                break;
            case 0xC000: // $C000-$DFFE IRQ latch
                irqLatch = value & 0xFF;
                break;
            case 0xC001: // $C001-$DFFF IRQ reload
                irqCounter = 0;
                break;
            case 0xE000: // $E000-$FFFE IRQ disable
                irqEnabled = false;
                generateIRQ = false;
                break;
            case 0xE001: // $E001-$FFFF IRQ enable
                irqEnabled = true;
                break;
            default:
                break;
        }
    }

    private void updateBanks(int selectedR) {
        switch (selectedR) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                switchCHRBanks();
                break;
            case 6:
            case 7:
                switchPRGBanks();
                break;
            default: break;
        }
    }

    private void switchCHRBanks() {
        for (int i = 0;i < 2;i++) {
            int v = r[i];
            chr2KBBanks.setMemory(0x800 * i, new DefaultMemory(loader.getCHRPageByIndex(v / 8)
                    , ((v >> 1) & 0x3) * 0x800, 0x800));
        }
        for (int i = 2;i < 6;i++) {
            int v = r[i];
            chr1KBBanks.setMemory(0x400 * (i - 2), new DefaultMemory(loader.getCHRPageByIndex(v / 8)
                    , (v & 0x7) * 0x400, 0x400));
        }
        if (!chrInversion) {
            chrMemory.setMemory(0, chr2KBBanks);
            chrMemory.setMemory(0x1000, chr1KBBanks);
        } else {
            chrMemory.setMemory(0, chr1KBBanks);
            chrMemory.setMemory(0x1000, chr2KBBanks);
        }
    }

    private void switchPRGBanks() {
        int r6PageId = r[6] & 0x3F;
        IMemory r6 = new ReadonlyMemory(loader.getPRGPageByIndex(r6PageId >> 1), (r6PageId & 1) * 0x2000, 0x2000);

        int r7PageId = r[7] & 0x3F;
        IMemory r7 = new ReadonlyMemory(loader.getPRGPageByIndex(r7PageId >> 1), (r7PageId & 1) * 0x2000, 0x2000);
        mainMemory.setMemory(0xA000, new MixedMemory(r7, 0, this, 0xA000, 0x2000));

        if (!prgBankMode) {
            mainMemory.setMemory(0x8000, new MixedMemory(r6, 0,
                    this, 0x8000, 0x2000));
            mainMemory.setMemory(0xC000, new MixedMemory(prgSecondLastBank,
                    0, this, 0xC000, 0x2000));
        } else {
            mainMemory.setMemory(0x8000, new MixedMemory(prgSecondLastBank,
                    0, this, 0x8000, 0x2000));
            mainMemory.setMemory(0xC000, new MixedMemory(r6, 0,
                    this, 0xC000, 0x2000));
        }
        mainMemory.setMemory(0xE000, new MixedMemory(prgLastBank, 0,
                this, 0xE000, 0x2000));
    }

    @Override
    public int getSize() {
        return 0x8000;
    }

    @Override
    public boolean getIRQLevel() {
        return generateIRQ;
    }
}
