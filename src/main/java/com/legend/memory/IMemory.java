package com.legend.memory;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description 内存接口
 */
public interface IMemory extends Serializable {

    /**
     * 从地址address读取1字节数据
     * @param address
     * @return
     */
    int readByte(int address);

    /**
     * 写1字节数据到地址address
     * @param address
     * @param value
     */
    void writeByte(int address, int value);
    int getSize();
}
