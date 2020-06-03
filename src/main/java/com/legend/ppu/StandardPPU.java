package com.legend.ppu;

import com.legend.cpu.ICPU;
import com.legend.memory.DefaultMemory;
import com.legend.memory.IMemory;
import com.legend.memory.MirrorMemory;
import com.legend.memory.StandardMemory;
import com.legend.screen.Screen;
import com.legend.storage.ISave;
import com.legend.utils.ByteUtils;

import java.io.*;
import java.util.Arrays;

import static com.legend.cartridges.INesLoader.*;

/**
 * @author Legend
 * @data by on 20-4-12.
 * @description PPU实现
 * idea:
 *      http://wiki.nesdev.com/w/index.php/PPU_scrolling
 */
public class StandardPPU implements IPPU {

    private static final long serialVersionUID = -8245041867994294340L;

    private PPURegister register = new PPURegister(this);
    private StandardMemory vram = new StandardMemory(0x10000);

    private IMemory sprMemory = new DefaultMemory(256);

    private IMemory[] splitNameTables = new IMemory[4]; // 背景页物理存储
    private StandardMemory nameTables = new StandardMemory(0x1000); // 背景页逻辑存储
    private IMemory patterns = new DefaultMemory(0x2000); // 字体库
    private IMemory paletteIndexes = new PaletteIndexes(); // 调色板索引

    private int scanline = 261;
    // 记录当前扫描线的时钟周期
    private int cycle = 0;

    /**
     * bit 0 = 1: is sprite 0, 0: not
     * bite 1 = 1: behind, 0: not
     */
    private byte[][] spriteAttributes = new byte[SCREEN_HEIGHT][SCREEN_WIDTH];
    private byte[][] spriteBuffer = new byte[SCREEN_HEIGHT][SCREEN_WIDTH];
    private byte[][] buffer = new byte[SCREEN_HEIGHT][SCREEN_WIDTH];

    private int sprite0HitCycle = -1;

    private int renderX = 0;
    private int renderY = 0;

    private int mirroringType = -1;

    public StandardPPU() {
        for (int i = 0;i < 4;i++) {
            splitNameTables[i] = new DefaultMemory(0x400);
            nameTables.setMemory(0x400 * i, splitNameTables[i]);
        }
        resetVRAMMemory();
    }

    private void resetVRAMMemory() {
        vram.setMemory(0, patterns);
        vram.setMemory(0x2000, nameTables);
        vram.setMemory(0x3000, new MirrorMemory(nameTables, 0xF00));
        vram.setMemory(0x3F00, paletteIndexes);
        vram.setMemory(0x3F20, new MirrorMemory(paletteIndexes, 0xE0));
        vram.setMemory(0x4000, new MirrorMemory(vram, 0xC000));
    }

    @Override
    public void setCHRMemory(IMemory chrRom) {
        this.patterns = chrRom;
        resetVRAMMemory();
    }

    @Override
    public IMemory getCHRMemory() {
        return patterns;
    }

    @Override
    public String getMirroringType() {
        switch (mirroringType) {
            case HORIZONTAL:
                return "HORIZONTAL";
            case VERTICAL:
                return "VERTICAL";
            case ONE_SCREEN_MIRRORING:
                return "ONE_SCREEN_MIRRORING";
            case FOUR_SCREEN_MIRRORING:
                return "FOUR_SCREEN_MIRRORING";
            default:
                return "Unknown";
        }
    }

    @Override
    public void setMirroringType(int mirroringType) {
//        System.out.println("镜像切换: " + mirroringType);
        setMirroringType(mirroringType, 0);
    }

    public void setMirroringType(int mirroringType, int singleLocation) {
        if (mirroringType == this.mirroringType) return;
        IMemory l1 = splitNameTables[0];
        IMemory l2 = splitNameTables[1];
        switch (mirroringType) {
            case HORIZONTAL:
                nameTables.setMemory(0x0, l1);
                nameTables.setMemory(0x400, l1);
                nameTables.setMemory(0x800, l2);
                nameTables.setMemory(0xC00, l2);
                break;
            case VERTICAL:
                nameTables.setMemory(0x0, l1);
                nameTables.setMemory(0x400, l2);
                nameTables.setMemory(0x800, l1);
                nameTables.setMemory(0xC00, l2);
                break;
            case ONE_SCREEN_MIRRORING:
                if (singleLocation == 0) {
                    for (int i = 0;i < 4;i++) {
                        nameTables.setMemory(i * 0x400, l1);
                    }
                } else if (singleLocation == 1){
                    for (int i = 0;i < 4;i++) {
                        nameTables.setMemory(i * 0x400, l2);
                    }
                }
                break;
            case FOUR_SCREEN_MIRRORING:
                nameTables.setMemory(0x0, l1);
                nameTables.setMemory(0x400, l2);
                nameTables.setMemory(0x800, splitNameTables[2]);
                nameTables.setMemory(0xC00, splitNameTables[3]);
                break;
            default:
                throw new IllegalArgumentException("The mirroringType is unknown!");
        }
        this.mirroringType = mirroringType;
        resetVRAMMemory();
    }

    @Override
    public PPURegister getRegister() {
        return register;
    }

    @Override
    public void setRegister(PPURegister register) {
        this.register = register;
    }

    @Override
    public IMemory getSprRAM() {
        return sprMemory;
    }

    @Override
    public StandardMemory getVRAM() {
        return vram;
    }

    @Override
    public IMemory getPalette() {
        return paletteIndexes;
    }

    @Override
    public void writeRegister(int index, int val) {
        switch (index) {
            case 3: // OAM ADDRESS
                register.setOAMData(sprMemory.readByte(val & 0xFF));
                break;
            case 4: // OAM DATA
                // 1.写数据到当前OAM地址 2.地址递增 3.递增后的数据读到0x2004
                sprMemory.writeByte(register.getOAMAddress(), val);
                register.increaseOAMAddress();
                register.setOAMData(sprMemory.readByte(register.getOAMAddress() & 0xFF));
                break;
            case 7: // PPU DATA
                vram.writeByte(register.getPPUAddress(), val);
                register.increasePPUAddress();
                register.setPPUData(vram.readByte(register.getPPUAddress()));
                break;
            default: break;
        }
    }

    @Override
    public int readRegister(int index) {
        int result = register.getData(index);
        switch (index) {
            case 2: // PPU STATUS
                register.clearVerticalBlank();
                break;
            case 7: // PPU DATA
                if (register.getPPUAddress() >= 0x3F00) {
                    // 0x3F00后是调色版索引地址的数据 位于数据总线
                    // 所以应该直接从VRAM内存里面读取 而不是从缓冲区
                    result = vram.readByte(register.getPPUAddress());
                }
                savePPUDataToRegister();
                register.increasePPUAddress();
                break;
            default: break;
        }
        return result;
    }

    private void savePPUDataToRegister() {
        register.setPPUData(vram.readByte(register.getPPUAddress()));
    }

    @Override
    public void cycle(Screen screen, ICPU cpu) {
        if (isVisibleRangeScanline()) {
            processVisibleScanline();
        } else if (scanline == 240) {
            startOfVBlank(cpu);
        } else if (scanline == 261) {
            preRenderScanline();
        }
        fillPixelByPalette(screen);
        increaseCycle();
    }

    private void processVisibleScanline() {
        if (!inHorizontalBlank() && (cycle & 7) == 0) {
            // 每8个周期一次渲染8个像素点
            if (register.isRenderingEnabled()) {
                renderBackgroundTile();
                if (cycle == 256) {
                    // 滚动到下一行 进行像素的预处理
                    register.increaseYScrollBy1();
                    if (scanline % 3 == 0) {
                        preRenderSprites();
                    }
                } else {
                    // 本行滚动处理像素
                    register.increaseXScrollBy8();
                }
            }
        }
        normalScanlineCommon();
    }

    // 0-239 261(-1)扫描线公共逻辑 261是一个虚拟的扫描线
    private void normalScanlineCommon() {
        if (cycle == 257) {
            if (register.isRenderingEnabled()) {
                // 从左到右一行渲染完 回到下一行的最左边
                register.updateTToV(0x41F); // updateX
                renderX = 0;
                renderY++;
                sprite0HitCycle = -1;
            }
        }
    }

    // 虚拟扫描线
    private void preRenderScanline() {
        if (cycle == 1) {
            endOfVBlank();
        } else if (cycle >= 280 && cycle <= 304) {
            if (register.isRenderingEnabled()) {
                // update Y
                register.updateTToV(0x7BE0);
            }
            preRenderSprites();
        }
        normalScanlineCommon();
    }

    private void endOfVBlank() {
        if (cycle == 1) {
            register.clearVerticalBlank();
            register.clearSprite0Hit();
            register.clearSpriteOverflow();
            renderY = -1;
        }
    }

    private void startOfVBlank(ICPU cpu) {
        if (cycle == 1) {
            register.setVerticalBlank();
            if (register.getGenerateNMI()) {
                cpu.nmi();
            }
        }
    }

    private void increaseCycle() {
        cycle++;
        if (cycle == 341) {
            cycle = 0;
            scanline++;
            if (scanline == 262) {
                scanline = 0;
            }
        }
    }

    /**
     * 将调色板索对应到屏幕的像素点 一次一个像素点
     */
    private void fillPixelByPalette(Screen screen) {
        if (scanline >= 0 && scanline < SCREEN_HEIGHT) {
            if (cycle > 0 && cycle <= SCREEN_WIDTH) {
                // 在扫描线的可见视图内
                if (register.isRenderingEnabled()) {
                    screen.set(cycle - 1, scanline, register.showBackground() ?
                            buffer[scanline][cycle-1] : spriteBuffer[scanline][cycle-1]);
                } else {
                    screen.set(cycle - 1, scanline, paletteIndexes.readByte(0));
                }
                if (register.showBackground() && register.showSprites()
                        && sprite0HitCycle == cycle) {
                    register.setSprite0Hit();
                }
            }
        }
    }

    @Override
    public boolean isVisibleRangeScanline() {
        return (scanline >= 0 && scanline < SCREEN_HEIGHT);
    }

    /***
     * 渲染背景像素块 一次渲染8个像素
     */
    private void renderBackgroundTile() {
        if (!register.showBackground() || renderY < 0
                || renderY >= SCREEN_HEIGHT) {
            // 渲染坐标在屏幕内并且背景显示被开启时才进行后续的渲染
//            System.out.println("renderBackgroundTile()--" + register.showBackground()
//                    + "--" + renderY);
            return;
        }
        int attribute = vram.readByte(register.getAttributeAddress());
        // 高两位 每一个基本像素块(8x8)共享2位
        int paletteAddress = getBgPaletteAddressHigh2Bit(attribute);

        int tileNumber = vram.readByte(register.getTileAddress());
        // 获取图案表中的像素块 图案表以16字节步进
        int patternAddress = register.getBackgroundPatternTableAddress() + (tileNumber << 4);
        // 低2位所在字节
        int y = register.getFineYScroll();
        int patternLow = vram.readByte(patternAddress + y);
        int patternHigh = vram.readByte(patternAddress + y + 8);

        int x = renderX - register.getFineXScroll();
        byte[] bufferLine = buffer[renderY];
        for (int i = x;i < x + 8;i++) {
            if (i >= 0 && i < SCREEN_WIDTH) {
                bufferLine[i] = -1;
            }
        }

        byte[] spriteBufferLine = spriteBuffer[renderY];
        byte[] spriteAttributeLine = spriteAttributes[renderY];
        byte backdropColor = (byte) paletteIndexes.readByte(0);

        for (int i = x;i < x + 8;i++) {
            if (i < SCREEN_WIDTH && (i >= 8 || (register.showLeftmost8PixelsBackground() && i >= 0))) {
                int index = 7 - (i - x);
                int v = ByteUtils.getBit(patternHigh, index) << 1 | ByteUtils.getBit(patternLow, index);
                if (v != 0) {
                    bufferLine[i] = (byte) paletteIndexes.readByte(paletteAddress | v);
                }
                if (register.showSprites() && spriteBufferLine[i] != -1) {
                    int attr = spriteAttributeLine[i];
                    if (sprite0HitCycle == -1 && (attr & 1) != 0
                            && bufferLine[i] != -1) {
                        // 标记精灵#0
                        sprite0HitCycle = i + 1;
                    }
                    // 背景当前像素点为空 或者 精灵不在背景后面
                    if (bufferLine[i] == -1 || (attr & 2) == 0) {
                        bufferLine[i] = spriteBufferLine[i];
                    }
                }
                if (bufferLine[i] == -1) {
                    bufferLine[i] = backdropColor;
                }
            }
        }
        renderX += 8;
    }

    private void preRenderSprites() {
        for (int j = 0;j < SCREEN_HEIGHT;j++) {
            for (int i = 0;i < SCREEN_WIDTH;i++) {
                spriteBuffer[j][i] = -1;
                spriteAttributes[j][i] = 0;
            }
        }
        int patternTableAddress = register.getSpritePatternTableAddress();
        boolean is8x16 = register.is8x16();
        for (int i = 256 - 4;i >= 0;i-=4) {
            int id = i >> 2;
            int y = sprMemory.readByte(i) + 1;
            if (y >= 240) continue; // 不在可见扫描线
            int tileNumber = sprMemory.readByte(i + 1);
            int attribute = sprMemory.readByte(i + 2);
            int x = sprMemory.readByte(i + 3);
            int paletteHigh = attribute & 3;
            boolean behindBackground = (attribute & 0x20) != 0;
            boolean flipHorizontally = (attribute & 0x40) != 0;
            boolean flipVertically = (attribute & 0x80) != 0;
            if (is8x16) {
                patternTableAddress = (tileNumber & 1) << 12;
                tileNumber &= 0xFE;
                if (flipVertically) {
                    preRenderSprite(id, x, y, patternTableAddress + ((tileNumber + 1) << 4),
                            0x10 + (paletteHigh << 2), behindBackground, flipHorizontally, true);
                    preRenderSprite(id, x, y + 8, patternTableAddress + (tileNumber << 4),
                            0x10 + (paletteHigh << 2), behindBackground, flipHorizontally, true);
                } else {
                    preRenderSprite(id, x, y, patternTableAddress + (tileNumber << 4),
                            0x10 + (paletteHigh << 2), behindBackground, flipHorizontally, false);
                    preRenderSprite(id, x, y + 8, patternTableAddress + ((tileNumber + 1) << 4),
                            0x10 + (paletteHigh << 2), behindBackground, flipHorizontally, false);
                }
            } else {
                preRenderSprite(id, x, y, patternTableAddress + (tileNumber << 4),
                        0x10 + (paletteHigh << 2), behindBackground,
                        flipHorizontally, flipVertically);
            }
        }
    }

    private void preRenderSprite(int id, int x, int y, int patternAddress,
                                 int paletteAddress, boolean behind,
                                 boolean flipH, boolean flipV) {
        if (x <= -8 || y <= -8 || x >= SCREEN_WIDTH || y >= SCREEN_HEIGHT) {
            return;
        }
        int offset = flipV ? 7 : 0;
        int incremental = flipV ? -1 : 1;
        // 绘制一个8x8像素的精灵
        for (int j = y;j < y + 8;j++) {
            if (j < 0 || j >= SCREEN_HEIGHT) continue;
            byte patternLow = (byte) patterns.readByte(patternAddress + offset);
            byte patternHigh = (byte) patterns.readByte(patternAddress + offset + 8);
            for (int i = x;i < x + 8;i++) {
                if (i < SCREEN_WIDTH && (i >= 8 || (register.showLeftmost8PixelsSprites() && i >= 0))) {
                    int index = i - x;
                    int v;
                    if (flipH) {
                        v = ByteUtils.getBit(patternHigh, index) << 1 | ByteUtils.getBit(patternLow, index);
                    } else {
                        index = 7 - index;
                        v = ByteUtils.getBit(patternHigh, index) << 1 | ByteUtils.getBit(patternLow, index);
                    }
                    if (v != 0) {
                        if (spriteBuffer[j][i] == -1 ||
                                !((behind && (spriteAttributes[j][i] & 2) == 0))) {
                            spriteBuffer[j][i] = (byte) paletteIndexes.readByte(paletteAddress | v);
                            spriteAttributes[j][i] = (byte) ((behind ? 2 : 0) | (spriteAttributes[j][i] & 1));
                        }
                        if (id == 0) {
                            spriteAttributes[j][i] |= 1;
                        }
                    }
                }
            }
            offset += incremental;
        }
    }


    private int getBgPaletteAddressHigh2Bit(int attribute) {
        int palette;
        boolean left = register.isPaletteLeft();
        boolean top = register.isPaletteTop();
        if (top && left) {
            palette = attribute & 3;
        } else if (top) {
            palette = (attribute >> 2) & 3;
        } else if (left) {
            palette = (attribute >> 4) & 3;
        } else {
            palette = (attribute >> 6) & 3;
        }
        return palette << 2;
    }

    @Override
    public void powerUp() {
        reset();
    }

    @Override
    public void reset() {
        register.reset();
        scanline = 261;
        cycle = 0;
    }

    @Override
    public boolean inVerticalBlank() {
        return scanline >= SCREEN_HEIGHT;
    }

    @Override
    public boolean inHorizontalBlank() {
        return cycle >= 257 && cycle <= 320;
    }

    @Override
    public int getScanline() {
        return scanline;
    }

    @Override
    public int getCycle() {
        return cycle;
    }

    @Override
    public void setVRAM(StandardMemory memory) {
        this.vram = memory;
    }

    @Override
    public void setSPRM(IMemory memory) {
        this.sprMemory = memory;
    }

    private class PaletteIndexes extends DefaultMemory {

        public PaletteIndexes() {
            super(0x20);
        }

        @Override
        public int readByte(int address) {
            int result = super.readByte(address);
            if (register.isGreyScale()) {
                return result & 0x30;
            }
            return result;
        }

        @Override
        public void writeByte(int address, int value) {
            super.writeByte(address, value);
            if (address == 0x10) {
                super.writeByte(0, value);
            }
        }
    }

}
