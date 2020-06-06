package com.legend.mapper;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.input.StandardControllers;
import com.legend.main.Emulator;
import com.legend.main.GameRunner;
import com.legend.memory.*;
import com.legend.ppu.IPPU;
import com.legend.storage.LocalStorage;
import com.legend.utils.ByteUtils;
import com.legend.utils.PropertiesUtils;
import com.legend.utils.StringUtils;

import java.io.*;

import static com.legend.utils.Constants.FIRST_RELOAD_FLAG;

/**
 * @author Legend
 * @data by on 20-6-2.
 * @description Mapper id = 163
 */
public class Mapper163 extends Mapper implements IMemory {

    private transient GameRunner runner;
    private transient INesLoader loader;
    private transient StandardMemory mainMemory;
    private transient IPPU ppu;
    private transient StandardMemory chrMemory = new StandardMemory(0x2000);
    private IMemory fixedCHRMemory = new DefaultMemory(0x2000);

    private boolean c;
    private int lastStrobe = 1;
    private int trigger = 0;
    private int idx = 0;
    private int[] reg = new int[8];


    @Override
    protected void mapMemoryImpl(GameRunner gameRunner) {
        this.runner = gameRunner;
        init(runner);
        mainMemory.setMemory(0x6000, new DefaultMemory(0x2000));
        switchBanks();
    }

    private void init(GameRunner gameRunner) {
        this.loader = gameRunner.getLoader();
        this.ppu = gameRunner.getPPU();
        this.mainMemory = (StandardMemory) gameRunner.getCPU().getMemory();
        mainMemory.setMemory(0x5000,  new MixedMemory(this,
                0x5000, this, 0x5000, 0x1000));
    }

    private void trick() {
        String key = FIRST_RELOAD_FLAG + runner.getLoader().getFileMD5();
        String firstReload = PropertiesUtils.get(key);
        boolean isNeedReload = StringUtils.isEmpty(firstReload)
                || Integer.parseInt(firstReload) == 0;
        if (isNeedReload && runner != null) {
            LocalStorage storage = new LocalStorage();
            runner.pause();
            storage.setPath("mapper163-trick" + ".trick");
            storage.save(runner);
            storage.load(runner);
            Emulator.controllers = (StandardControllers) ((StandardMemory) runner.getCPU()
                    .getMemory()).getMemory(0x4016);
            System.out.println("重载完成");
            runner.resume();
        }
    }

    @Override
    public void cycle(ICPU cpu) {
        int scanline = ppu.getScanline();
        int targetCycle = 320;
        boolean inCycle = ppu.getCycle() >= targetCycle
                && ppu.getCycle() <= (targetCycle + 3);
        if (c && inCycle) {
            trick();
            if (scanline == 127) {
                switchCHRBank(1);
            } else if (scanline == 239) {
                switchCHRBank(0);
            }
        }
    }

    @Override
    public int readByte(int address) {
//        System.out.println(String.format("%04x", address));
        switch (address & 0x7700) {
            case 0x5100:
                return reg[2] | reg[0] | reg[1] | reg[3] ^ 0xff;
            case 0x5500:
                if (trigger == 1) {
                    return reg[2] | reg[1];
                } else {
                    return 0;
                }
            default: break;
        }
        return 4;
    }

    @Override
    public void writeByte(int address, int value) {
//        System.out.println(String.format("%04x---%04x", address, value));
        if (address == 0x5101) {
            if (lastStrobe != 0 && value == 0) {
                trigger ^= 1;
            }
            lastStrobe = value;
        } else if (address == 0x5100 && value == 6) {
            mainMemory.setMemory(0x8000, new ReadonlyMemory(loader.getPRGPageByIndex(3 * 2)));
            mainMemory.setMemory(0xC000, new ReadonlyMemory(loader.getPRGPageByIndex(3 * 2 + 1)));
        } else {
            switch (address & 0x7300) {
                case 0x5200:
                    reg[0] = value;
                    switchBanks();
                    break;
                case 0x5000:
                    reg[1] = value;
                    c = ByteUtils.getBit(value, 7) != 0;
                    switchBanks();
                    break;
                case 0x5300:
                    reg[2] = value;
                    break;
                case 0x5100:
                    reg[3] = value;
                    switchBanks();
                    break;

            }
        }
    }

    private void switchBanks() {
        ppu.setCHRMemory(fixedCHRMemory);
        switchPRGBank();
    }

    private void switchCHRBank(int idx) {
        chrMemory.setMemory(0, new MirrorMemory(fixedCHRMemory, idx * 0x1000, 0x1000));
        chrMemory.setMemory(0x1000, new MirrorMemory(fixedCHRMemory, idx * 0x1000, 0x1000));
        ppu.setCHRMemory(chrMemory);
        this.idx = idx;
    }

    private void switchPRGBank() {
        int bank = (reg[0] << 4) | (reg[1] & 0xF);
        mainMemory.setMemory(0x8000, new ReadonlyMemory(loader.getPRGPageByIndex(bank * 2)));
        mainMemory.setMemory(0xC000, new ReadonlyMemory(loader.getPRGPageByIndex(bank * 2 + 1)));
    }

    @Override
    public byte[] getSaveBytes() throws IOException {
        String str = JSONUtil.toJsonStr(reg);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream bos = new ObjectOutputStream(baos);
        bos.writeObject(fixedCHRMemory);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(idx);
        dos.writeBoolean(c);
        dos.writeInt(lastStrobe);
        dos.writeInt(trigger);
        dos.writeUTF(str);
        dos.flush();
        dos.close();
        return baos.toByteArray();
    }

    @Override
    public void reload(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        try {
            this.fixedCHRMemory = (IMemory) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        DataInputStream dis = new DataInputStream(ois);
        this.idx = dis.readInt();
        this.c = dis.readBoolean();
        this.lastStrobe = dis.readInt();
        this.trigger = dis.readInt();
        String str = dis.readUTF();
        JSONArray array = JSONUtil.parseArray(str);
        for (int i = 0;i < reg.length;i++) {
            reg[i] = (int) array.get(i);
        }
        init(runner);
        switchBanks();
        switchCHRBank(idx);
        dis.close();
        bais.close();
    }

    @Override
    public int getSize() {
        return 0x1000;
    }
}