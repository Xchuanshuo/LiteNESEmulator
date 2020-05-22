package com.legend.storage;

import cn.hutool.core.util.ZipUtil;
import com.legend.apu.IAPU;
import com.legend.cpu.CPURegister;
import com.legend.cpu.ICPU;
import com.legend.main.GameRunner;
import com.legend.mapper.Mapper;
import com.legend.memory.IMemory;
import com.legend.ppu.IPPU;

import java.io.*;

/**
 * @author Legend
 * @data by on 20-5-3.
 * @description 游戏本地即时存档与读取实现
 */
public class LocalStorage implements IStorage {

    private String path = "test1.sl";


    public LocalStorage() {}

    public void setPath(String path) {
        if (path.lastIndexOf(".") == -1) {
            path += ".sl";
        }
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean save(GameRunner runner) {
        try {
            ICPU cpu = runner.getCPU();
            IPPU ppu = runner.getPPU();
            IAPU apu = runner.getAPU();
            Mapper mapper = runner.getMapper();
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(cpu.getRegister());
            oos.writeObject(cpu.getMemory());
            oos.writeObject(ppu);
            oos.writeObject(apu);
            oos.writeObject(mapper.getSaveBytes());
            byte[] bytes = ZipUtil.gzip(baos.toByteArray());
            oos.flush();
            oos.close();
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void load(GameRunner gameRunner) {
        try {
            ICPU cpu = gameRunner.getCPU();
            FileInputStream fis = new FileInputStream(path);
            InputStream inputStream = new ByteArrayInputStream(ZipUtil.unGzip(fis));
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            // restore cpu
            CPURegister cpuRegister = (CPURegister) ois.readObject();
            IMemory mainMemory = (IMemory) ois.readObject();
            cpu.setRegister(cpuRegister);
            cpu.setMemory(mainMemory);

            // restore ppu
            IPPU ippu = (IPPU) ois.readObject();
            gameRunner.setPpu(ippu);

            // restore apu
            IAPU iapu = (IAPU) ois.readObject();
            gameRunner.setApu(iapu);

            // restore Mapper
            byte[] mapperData = (byte[]) ois.readObject();
            gameRunner.getMapper().reload(mapperData);

            ois.close();
            inputStream.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
