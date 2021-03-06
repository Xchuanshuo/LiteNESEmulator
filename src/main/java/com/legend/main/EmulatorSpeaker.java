package com.legend.main;

import com.legend.speaker.DefaultSpeaker;
import com.legend.speaker.Speaker;

import javax.sound.sampled.*;

/**
 * @author Legend
 * @data by on 20-5-3.
 * @description
 */
public class EmulatorSpeaker implements Runnable {

    private volatile boolean isStop = false;

    private final int sampleRate;
    private Speaker speaker = new DefaultSpeaker();

    public EmulatorSpeaker() {
        this(Emulator.SPEAKER_SAMPLE_RATE);
    }

    public EmulatorSpeaker(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    @Override
    public void run() {
        this.isStop = false;
        try {
            runImpl();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void runImpl() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open(format);
        sourceDataLine.start();

        while (!isStop) {
            byte[] bytes = speaker.output();
            if (isStop) break;
            sourceDataLine.write(bytes, 0, bytes.length);
        }

        sourceDataLine.drain();
        sourceDataLine.close();
    }

    public boolean isStop() {
        return isStop;
    }

    public void stop() {
        this.isStop = true;
        speaker.reset();
    }
}
