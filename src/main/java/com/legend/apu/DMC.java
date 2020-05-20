package com.legend.apu;

import com.legend.cpu.ICPU;
import com.legend.cpu.IRQGenerator;
import com.legend.utils.ByteUtils;


/**
 * @author Legend
 * @data by on 20-5-2.
 * @description 差值调制通道
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_DMC
 */
public class DMC implements SoundGenerator, DividerListener, IRQGenerator {

    private static final int[] RATE_MAPPER = {
            428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106,  84,  72,  54
    };

    private Divider timer = new DecrementDivider();
    private boolean interruptFlag;
    private boolean irqEnable;
    private boolean loopFlag;
    private int sampleAddress;
    private int sampleLength;

    private int currentSampleAddress;
    private int bytesRemaining;
    private boolean sampleBufferEmpty = true;
    private int sampleBuffer;

    private boolean silenceFlag;
    private int shiftRegister;
    private int bitsRemainingCounter;
    private int outputLevel;
    private ICPU cpu;
    private boolean evenCycle;

    public DMC() {
        timer.setOutputClock(this);
    }

    @Override
    public void onClock(Divider divider) {
        if (bitsRemainingCounter == 0) {
            bitsRemainingCounter = 8;
            reloadSampleBuffer();
            if (sampleBufferEmpty) {
                silenceFlag = true;
            } else {
                silenceFlag = false;
                shiftRegister = sampleBuffer;
                sampleBufferEmpty = true;
            }
        }
        if (!silenceFlag) {
            int bit0 = shiftRegister & 1;
            if (bit0 == 1 && outputLevel <= 125) {
                outputLevel += 2;
            } else if (bit0 == 0 && outputLevel >= 2) {
                outputLevel -= 2;
            }
        }
        shiftRegister >>= 1;
        bitsRemainingCounter--;
    }

    private void reloadSampleBuffer() {
        if (!sampleBufferEmpty || bytesRemaining == 0) {
            return;
        }
        sampleBuffer = cpu.getMemory().readByte(currentSampleAddress);
        sampleBufferEmpty = false;
        currentSampleAddress++;
        if (currentSampleAddress == 0x10000) {
            currentSampleAddress = 0x8000;
        }
        bytesRemaining--;
        if (bytesRemaining == 0) {
            if (loopFlag) {
                restart(false);
            } else if (irqEnable) {
                interruptFlag = true;
            }
        }
    }

    @Override
    public void cycle(ICPU cpu) {
        evenCycle = !evenCycle;
        if (!evenCycle) return;
        this.cpu = cpu;
        timer.clock();
        this.cpu = null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.interruptFlag = false;
        if (!enabled) {
            this.bytesRemaining = 0;
        }
        if (enabled && bytesRemaining == 0) {
            restart(true);
        }
    }

    private void restart(boolean bufferEmpty) {
        currentSampleAddress = sampleAddress;
        bytesRemaining = sampleLength;
        sampleBufferEmpty = bufferEmpty;
    }

    @Override
    public void setRegister(int index, int value) {
        switch (index) {
            case 0: // 0x4010
                setIRQEnabled(ByteUtils.getBit(value, 7) != 0);
                loopFlag = ByteUtils.getBit(value, 6) != 0;
                int idx = ByteUtils.getBitsByRange(value, 0, 3);
                int period = RATE_MAPPER[idx];
                timer.setPeriod(period);
                break;
            case 1: // 0x4011
                outputLevel = ByteUtils.getBitsByRange(value, 0, 6);
                break;
            case 2: // 0x4012
                // Sample address = %11AAAAAA.AA000000 = $C000 + (A * 64)
                sampleAddress = 0xC000 + ((value & 0xFF) << 6);
                break;
            case 3: // 0x4013
                // Sample length = %LLLL.LLLL0001 = (L * 16) + 1 bytes
                sampleLength = (value & 0xFF) << 4 + 1;
                break;
        }
    }

    @Override
    public int output() {
        return outputLevel;
    }

    @Override
    public boolean isActive() {
        return bytesRemaining > 0;
    }

    public void setIRQEnabled(boolean enabled) {
        irqEnable = enabled;
        if (!irqEnable) {
            interruptFlag = false;
        }
    }

    public boolean getInterruptFlag() {
        return interruptFlag;
    }

    @Override
    public void clockLengthCounterAndSweep() {}

    @Override
    public void clockEnvelopeAndLinearCounter() {}

    @Override
    public boolean getIRQLevel() {
        return interruptFlag;
    }

}
