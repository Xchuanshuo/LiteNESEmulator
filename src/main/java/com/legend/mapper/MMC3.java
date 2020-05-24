package com.legend.mapper;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.legend.apu.IAPU;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.cpu.IRQGenerator;
import com.legend.input.Input;
import com.legend.main.GameRunner;
import com.legend.memory.*;
import com.legend.ppu.IPPU;
import com.legend.ppu.PPURegister;
import com.legend.storage.ISave;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Legend
 * @data by on 20-5-5.
 * @description Mapper id = 4
 *  VROM = CHR-ROM
 *  idea:
 *      https://wiki.nesdev.com/w/index.php/MMC3
 */
public class MMC3 extends Mapper implements IMemory, IRQGenerator {

    private static final long serialVersionUID = -1936067312510465785L;

    private transient GameRunner runner;
    private transient IPPU ppu;
    private transient INesLoader loader;
    private transient StandardMemory mainMemory;
    private int irqCounter;
    private int irqLatch;
    private boolean irqReload = false;
    private boolean irqEnabled = false;
    private boolean generateIRQ = false;
    private transient IMemory prgSecondLastBank, prgLastBank;

    private transient StandardMemory chrMemory = new StandardMemory(0x2000);
    private transient StandardMemory chr2KBBanks = new StandardMemory(0x1000);
    private transient StandardMemory chr1KBBanks = new StandardMemory(0x1000);
    private boolean prgBankMode = false;
    private boolean chrInversion = true;
    private int selectedR;

    private int[] r = new int[8];

    @Override
    protected void mapMemoryImpl(GameRunner runner) {
        this.runner = runner;
        init();
        this.prgSecondLastBank = new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1)
                , 0, 0x2000);
        this.prgLastBank = new ReadonlyMemory(loader.getPRGPageByIndex(loader.getPRGPageCount() - 1)
                , 0x2000, 0x2000);
        resetR();
        switchCHRBanks();
        switchPRGBanks();
    }

    private void init() {
        this.mainMemory = (StandardMemory) runner.getCPU().getMemory();
        this.loader = runner.getLoader();
        this.ppu = runner.getPPU();
        if (loader.getFileMD5().equals("483695DE094F4DD49652C5BC78BDBA19") ||
                loader.getFileMD5().equals("B7F85F46191ACAD2765F2FB1C656F042")) {
            // 忍者神龟3, 超级玛丽3
            this.prgBankMode = true;
        }
    }

    private void resetR() {
        r[0] = 0;
        r[1] = 2;
        r[2] = 4;
        r[3] = 5;
        r[4] = 6;
        r[5] = 7;
        r[6] = 0;
        r[7] = 1;
    }

    @Override
    public void cycle(ICPU cpu) {
        PPURegister r = ppu.getRegister();
        if (r.getSpritePatternTableAddress() == r.getBackgroundPatternTableAddress()) {
            return;
        }
        int scanLine = ppu.getScanline();
        int cycle = ppu.getCycle();
        int cyclePos = r.getBackgroundPatternTableAddress() == 0 ? 260 : 324;
        if ((cycle >= cyclePos && cycle <= cyclePos + 2)
                && (ppu.isVisibleRangeScanline() || scanLine == 261)
                && (r.showBackground() || r.showSprites())) {
            if (irqCounter == 0 || irqReload) {
                irqCounter = irqLatch;
                irqReload = false;
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
//        System.out.println(String.format("0x%04X : 0x%04X", address, value));
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
                irqReload = true;
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
                    , (v % 8) * 0x400, 0x800));
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
        ppu.setCHRMemory(chrMemory);
//        System.out.println("1-CHR切换: r0: " + r[0]+ ", r1: " + r[1] +", r2: "+ r[2] + ", r3: " + r[3]
//                + ", r4: " + r[4] + ", r5: " + r[5] + " --chrInversion:" + chrInversion);
    }

    private void switchPRGBanks() {
//        System.out.println("2-PRG切换: r6: " + r[6] +"--r7: "+ r[7] + " prgBankMode-" + prgBankMode);
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
        if (generateIRQ) {
//            System.out.println("MMC3 IRQ------");
            generateIRQ = false;
            return true;
        }
        return false;
    }

    @Override
    public byte[] getSaveBytes() throws IOException {
        String str = JSONUtil.toJsonStr(r);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeBoolean(prgBankMode);
        dos.writeBoolean(chrInversion);
        dos.writeInt(selectedR);
        dos.writeUTF(str);
        dos.flush();
        dos.close();
        return baos.toByteArray();
    }

    @Override
    public void reload(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bais);
        this.prgBankMode = dis.readBoolean();
        this.chrInversion = dis.readBoolean();
        this.selectedR = dis.readInt();
        String str = dis.readUTF();
        JSONArray array = JSONUtil.parseArray(str);
        for (int i = 0;i < r.length;i++) {
            r[i] = (int) array.get(i);
        }
        init();
        switchCHRBanks();
        switchPRGBanks();
        dis.close();
        bais.close();
    }
}
