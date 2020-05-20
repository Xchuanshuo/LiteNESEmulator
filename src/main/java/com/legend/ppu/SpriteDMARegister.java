package com.legend.ppu;

import com.legend.cpu.ICPU;
import com.legend.memory.IMemory;

/**
 * @author Legend
 * @data by on 20-4-12.
 * @description 主内存地址$4014映射到Sprite DMA的IO寄存器, 用来直接搬运主内存的的数据到SPRAM
 */
public class SpriteDMARegister implements IMemory {

    private final ICPU cpu;
    private final IMemory mainMemory;
    private final IMemory sprRAM;

    public SpriteDMARegister(IMemory mainMemory, IMemory sprRam, ICPU cpu) {
        this.mainMemory = mainMemory;
        this.sprRAM = sprRam;
        this.cpu = cpu;
    }

    @Override
    public int readByte(int address) {
        return 0;
    }

    @Override
    public void writeByte(int address, int value) {
        cpu.increaseCycle(513);
        // value是数据页 256字节数据是来自主内存的$xx00-$xxFF拷贝
        value = (value & 0xFF) << 8;
        for (int i = 0;i < 256;i++) {
            sprRAM.writeByte(i, mainMemory.readByte(value + i));
        }
    }

    @Override
    public int getSize() {
        return 1;
    }
}
