package com.legend.storage;

import com.legend.apu.IAPU;
import com.legend.cpu.ICPU;
import com.legend.main.GameRunner;
import com.legend.mapper.Mapper;
import com.legend.ppu.IPPU;

/**
 * @author Legend
 * @data by on 20-5-3.
 * @description 存档接口类
 */
public interface IStorage {

    boolean save(GameRunner runner);

    void load(GameRunner runner);
}
