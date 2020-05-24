package com.legend.speaker;

import com.legend.main.Emulator;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Legend
 * @data by on 20-5-3.
 * @description 存储声音信号和输出
 */
public class DefaultSpeaker implements Speaker {

    private final double cyclePerSample;
    private final Queue<byte[]> queue = new LinkedList<>();
    private final int bufferSize;
    private byte[] buffer;
    private long cycle = 0;
    private int bufferId = 0;
    private int sampleRate;

    public DefaultSpeaker() {
        this(Emulator.SPEAKER_SAMPLE_RATE);
    }

    public DefaultSpeaker(int sampleRate) {
        this.sampleRate = sampleRate;
        this.cyclePerSample = Emulator.CPU_CYCLE_PER_SECOND / sampleRate;
        this.bufferSize = sampleRate / 441;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public void set(int level) {
        byte l = (byte) (level - 64);
        int bufferPos = (int) (cycle / cyclePerSample);
        int newBufferId = bufferPos / bufferSize;
        if (newBufferId != bufferId) {
            enqueue(buffer);
            this.buffer = new byte[bufferSize];
            bufferId = newBufferId;
        }
        buffer[bufferPos % bufferSize] = l;
        if (queue.size() < 2) {
            cycle++;
        }
    }

    public int getSampleRate() {
        return sampleRate;
    }

    @Override
    public byte[] output() {
        return dequeue();
    }

    @Override
    public void reset() {
        this.cycle = 0;
        this.bufferId = 0;
        // 重置时需要唤醒阻塞
        enqueue(new byte[bufferSize]);
    }


    private void enqueue(byte[] data) {
        synchronized (queue) {
            queue.offer(data);
            queue.notifyAll();
        }
    }

    private byte[] dequeue() {
        synchronized (queue) {
            while (queue.isEmpty()) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return queue.poll();
        }
    }
}
