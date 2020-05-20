package com.legend.apu;

import com.legend.cpu.ICPU;
import com.legend.speaker.Speaker;

/**
 * @author Legend
 * @data by on 20-5-3.
 * @description APU实现类
 * idea:
 *      https://wiki.nesdev.com/w/index.php/APU_Mixer
 */
public class StandardAPU implements IAPU {

    private static final long serialVersionUID = 7920061744943321241L;
    private Pulse pulse1 = new Pulse(1);
    private Pulse pulse2 = new Pulse(2);
    private Triangle triangle = new Triangle();
    private Noise noise = new Noise();
    private DMC dmc = new DMC();

    private SoundGenerator[] generators = {pulse1, pulse2, triangle, noise, dmc};
    private APURegister r = new APURegister(generators);

    @Override
    public APURegister getRegister() {
        return r;
    }

    @Override
    public void setRegister(APURegister register) {
        this.r = register;
    }

    @Override
    public void writeRegister(int index, int value) {}

    @Override
    public int readRegister(int index) {
        return r.readByte(index);
    }

    @Override
    public void cycle(Speaker speaker, ICPU cpu) {
        int oldPeriod = r.frameCounterTimer / APURegister.FRAME_COUNTER_PERIOD;
        int newPeriod = (r.frameCounterTimer+1) / APURegister.FRAME_COUNTER_PERIOD;
        if (oldPeriod != newPeriod) {
            int stepCount = r.getStepMode();
            if ((newPeriod >= 1 && newPeriod <= 3) || newPeriod == stepCount) {
                for (SoundGenerator generator : generators) {
                    generator.clockEnvelopeAndLinearCounter();
                }
            }
            if (newPeriod == 2 || newPeriod == stepCount) {
                for (SoundGenerator generator : generators) {
                    generator.clockLengthCounterAndSweep();
                }
            }
            if (!r.isInterruptDisable() && newPeriod == stepCount && stepCount == 4) {
                r.setStatusFrameCounterInterrupt();
            }
            if (newPeriod == stepCount) {
                r.frameCounterTimer = -2;
            }
        }
        for (SoundGenerator generator : generators) {
            generator.cycle(cpu);
        }

        double pulseOut = 95.88 / ((8128.0 / (pulse1.output() + pulse2.output())) + 100);
        double tndOut = 159.79 / (100 + 1 / ((triangle.output() / 8227.0)
                + (noise.output() / 12241.0) + (dmc.output() / 22638.0)));
        double output = pulseOut + tndOut;

        if (output < 0) output = 0;
        if (output > 1) output = 1;
        speaker.set((int) (output * 255));

        r.frameCounterTimer++;
    }

    @Override
    public void powerUp() {
        reset();
    }

    @Override
    public void reset() {
        r.reset();
    }

    @Override
    public boolean getIRQLevel() {
        return r.getStatusFrameCounterInterrupt() || dmc.getIRQLevel();
    }
}
