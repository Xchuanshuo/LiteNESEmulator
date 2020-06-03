package com.legend.cartridges;

import com.legend.utils.ByteUtils;
import com.legend.utils.MD5Util;

import java.io.*;
import java.util.Arrays;

/**
 * @author Legend
 * @data by on 20-4-4.
 * @description
 */
public class InputStreamNesLoader implements INesLoader {

    private int mapper;
    private int prgPageCount, chrPageCount;
    private byte[][] prgPages, chrPages;
    private int mirroringDirection;
    private boolean isSRAMEnable;
    private boolean is512ByteTrainerPresent;
    private boolean isFourScreenMirroring;
    private byte[] trainer;
    private String md5;

    public InputStreamNesLoader(InputStream inputStream) throws IOException {
        DataInputStream dis = new DataInputStream(inputStream);
        byte[] head = new byte[16];
        // 首先读取16字节的头部数据
        dis.readFully(head);
        if (head[0] != 'N' && head[1] != 'E' && head[2] != 'S' && head[3] != 0x1A) {
            throw new IOException("Not a nes file!");
        }
        prgPageCount = head[4] & 0xFF;
        chrPageCount = head[5] & 0xFF;
        byte romControl1 = head[6];
        byte romControl2 = head[7];
        // 需要注意大/小端序的问题 6502CPU使用的是小端序(较小的值存储在低地址)
        mirroringDirection =  ByteUtils.getBit(romControl1, 0);
        isSRAMEnable = ByteUtils.getBit(romControl1, 1) == 1;
        is512ByteTrainerPresent = ByteUtils.getBit(romControl1, 2) == 1;
        isFourScreenMirroring = ByteUtils.getBit(romControl1, 3) == 1;
        mapper = ByteUtils.getBitsByRange(romControl1, 4, 7)
                | (ByteUtils.getBitsByRange(romControl2, 4, 7) << 4);
        // 读取剩余的数据
        if (is512ByteTrainerPresent) {
            this.trainer = new byte[512];
            dis.readFully(trainer);
        } else {
            this.trainer = new byte[0];
        }
        this.prgPages = new byte[prgPageCount][16 * 1024];
        this.chrPages = new byte[chrPageCount][8 * 1024];
        for (int i = 0;i < prgPageCount;i++) {
            dis.readFully(prgPages[i]);
        }
        for (int i = 0;i < chrPageCount;i++) {
            dis.readFully(chrPages[i]);
        }
        dis.close();
    }

    @Override
    public int getPRGPageCount() {
        return prgPageCount;
    }

    @Override
    public int getCHRPageCount() {
        return chrPageCount;
    }

    @Override
    public byte[] getPRGPageByIndex(int index) {
        return prgPages[index % prgPageCount];
    }

    @Override
    public byte[] getCHRPageByIndex(int index) {
        if (chrPageCount == 0) return new byte[8 * 1024];
        return chrPages[index % chrPageCount];
    }

    @Override
    public int getMirroringDirection() {
        return mirroringDirection;
    }

    @Override
    public boolean isSRAMEnable() {
        return isSRAMEnable;
    }

    @Override
    public void setSRAMEnable(boolean enable) {
        this.isSRAMEnable = enable;
    }

    @Override
    public boolean is512ByteTrainerPresent() {
        return is512ByteTrainerPresent;
    }

    @Override
    public boolean isFourScreenMirroring() {
        return isFourScreenMirroring;
    }

    @Override
    public int getMapper() {
        return mapper;
    }

    @Override
    public byte[] getTrainer() {
        return trainer;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String getFileMD5() {
        return md5;
    }

    @Override
    public String toString() {
        return "InputStreamNesLoader{" +
                "mapperId=" + mapper +
                ", prgPageCount=" + prgPageCount +
                ", chrPageCount=" + chrPageCount +
                ", mirroringDirection=" + mirroringDirection +
                ", isSRAMEnable=" + isSRAMEnable +
                ", is512ByteTrainerPresent=" + is512ByteTrainerPresent +
                ", isFourScreenMirroring=" + isFourScreenMirroring +
                ", trainer=" + Arrays.toString(trainer) +
                ", MD5=" + md5 +
                '}';
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/home/legend/Projects/IdeaProjects/" +
                "2020/LiteNESEmulator/resources/game1.nes");
        InputStreamNesLoader nesLoader = new InputStreamNesLoader(new FileInputStream(file));
        System.out.println(nesLoader);
    }
}
