package com.legend.main;

import com.legend.apu.IAPU;
import com.legend.apu.StandardAPU;
import com.legend.cartridges.FileNesLoader;
import com.legend.cartridges.INesLoader;
import com.legend.cpu.ICPU;
import com.legend.cpu.StandardCPU;
import com.legend.input.Input;
import com.legend.mapper.Mapper;
import com.legend.mapper.MapperFactory;
import com.legend.ppu.IPPU;
import com.legend.ppu.StandardPPU;
import com.legend.screen.Screen;
import com.legend.speaker.Speaker;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description 游戏线程
 */
@Slf4j
public class GameRunner implements Runnable {

    // debug
    private boolean isStepInto = false;
    private boolean isEnableDebug = false;
    private Set<Integer> breakPointers = new HashSet<>();

    private volatile boolean stop = false;
    private volatile boolean pause = false;
    private final byte[] pauseLock = new byte[0];

    private final INesLoader loader;
    private ICPU cpu = new StandardCPU();
    private IPPU ppu = new StandardPPU();
    private IAPU apu = new StandardAPU();
    private Speaker speaker;
    private final Screen screen;
    private final Input input;
    private final Runnable repaintListener;
    private Mapper mapper;

    private double fps = 60;
    private double cps = 1.7e6;
    private long waitTime = 0;
    private long frame = 0;
    private long startTime = 0;
    private long oldCycle = 0;


    public GameRunner(String filepath, Input input, Screen screen, Speaker speaker, Runnable repaintListener) throws IOException {
        this.loader = new FileNesLoader(filepath);
        this.screen = screen;
        this.speaker = speaker;
        this.input = input;
        this.repaintListener = repaintListener;
    }

    @Override
    public void run() {
        this.mapper = MapperFactory.createMapperFromId(loader.getMapper());
        mapper.mapMemory(this);

        apu.powerUp();
        ppu.powerUp();
        cpu.powerUp();

        initCycle();
        boolean oldInBlank = false;
        while (!stop) {
            for (int k = 0; k < 100; k++) {
                long lastTime = System.nanoTime();
                processDebug();
                if (pause) {
                    synchronized (pauseLock) {
                        try {
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    waitTime += System.nanoTime() - lastTime;
                }
                int cycle = (int) (cpu.execute() - oldCycle);
                oldCycle = cpu.getCycle();
                for (int j = 0; j < cycle; j++) {
                    mapper.cycle(cpu);
                    apu.cycle(speaker, cpu);
                    ppu.cycle(screen, cpu);
                    ppu.cycle(screen, cpu);
                    ppu.cycle(screen, cpu);
                    if (!oldInBlank && ppu.inVerticalBlank()) {
                        repaintListener.run();
                        frame++;
                    }
                    oldInBlank = ppu.inVerticalBlank();
                }
            }
            long timeDiff = System.nanoTime() - startTime - waitTime;
            fps = frame * 1e9 / timeDiff;
            cps = cpu.getCycle() * 1e9 / timeDiff;
            while (cps > Emulator.CPU_CYCLE_PER_SECOND) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cps = cpu.getCycle() * 1e9 / (System.nanoTime() - startTime - waitTime);
            }
        }
    }

    public void onReset() {
        pause();
        apu.reset();
        ppu.reset();
        cpu.reset();
        mapper.mapMemory(this);
        cpu.increaseCycle((int) oldCycle);
        resume();
    }

    public void initCycle() {
        startTime = System.nanoTime();
        waitTime = 0;
        cpu.increaseCycle((int) -oldCycle);
        oldCycle = 0;
        frame = 0;
    }

    private void processDebug() {
        if (isStepInto || breakPointers.contains(cpu.getRegister().getPC())) {
            if (breakPointers.contains(cpu.getRegister().getPC())) {
                repaintListener.run();
            }
            pause = true;
        }
    }

    public ICPU getCPU() {
        return cpu;
    }

    public IPPU getPPU() {
        return ppu;
    }

    public IAPU getAPU() {
        return apu;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public void stop() {
        stop = true;
    }

    public void pause() {
        synchronized (pauseLock) {
            pause = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            pause = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isStop() {
        return stop;
    }

    public void setCpu(ICPU cpu) {
        this.cpu = cpu;
    }

    public void setApu(IAPU apu) {
        this.apu = apu;
    }

    public INesLoader getLoader() {
        return loader;
    }

    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public void setPpu(IPPU ppu) {
        this.ppu = ppu;
    }

    public Input getInput() {
        return input;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public double getCps() {
        return cps;
    }

    public double getFps() {
        return Math.round(fps*100) / 100.0;
    }

    public void setStepInto(boolean stepInto) {
        isStepInto = stepInto;
    }

    public boolean isEnableDebug() {
        return isEnableDebug;
    }

    public void setEnableDebug(boolean enableDebug) {
        isEnableDebug = enableDebug;
    }

    public Set<Integer> getBreakPointers() {
        return breakPointers;
    }
}
