package com.legend.cpu;

import com.legend.memory.IMemory;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-5.
 * @description CPU接口
 */
public interface ICPU extends Serializable {

    CPURegister getRegister();
    void setRegister(CPURegister register);
    void setMemory(IMemory memory);
    IMemory getMemory();
    long execute();
    long getCycle();
    void increaseCycle(int value);
    void increasePC();
    void reset();
    void powerUp();
    void nmi();
    // 只有irq能通过设置标志位屏蔽 reset/nmi/brk都是不能被该标志位屏蔽的
    void irq();
    void addIRQGenerator(IRQGenerator generator);
}
