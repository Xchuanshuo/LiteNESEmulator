package com.legend.ppu;

import com.legend.cpu.ICPU;
import com.legend.memory.IMemory;
import com.legend.memory.StandardMemory;
import com.legend.screen.Screen;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-12.
 * @description PPU接口
 */
public interface IPPU extends Serializable {

    int SCREEN_WIDTH = 256;
    int SCREEN_HEIGHT = 240;

    void setCHRMemory(IMemory chrRom);
    void setMirroringType(int mirroringType);
    // 单屏镜像名称表的位置
    void setMirroringType(int mirroringType, int singleLocation);

    PPURegister getRegister();
    void setRegister(PPURegister register);
    IMemory getSprRAM();
    StandardMemory getVRAM();
    IMemory getPalette();

    void writeRegister(int index, int val);
    int readRegister(int index);

    void cycle(Screen screen, ICPU cpu);
    void powerUp();
    void reset();

    boolean inVerticalBlank();
    boolean inHorizontalBlank();

    /**
     * 扫描线是否在可见的范围内
     */
    boolean isVisibleRangeScanline();

    int getScanline();
    int getCycle();

    void setVRAM(StandardMemory memory);
    void setSPRM(IMemory memory);
}
