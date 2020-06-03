package com.legend.apu;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-5-1.
 * @description 序列器(三角波中用来输出音频的信号 方波中用来获取占空周期)
 */
public class Sequencer implements Serializable {
    private int[] sequence;
    private int sequenceCounter;

    public Sequencer() {}

    public Sequencer(int[] sequence) {
        this.sequence = sequence;
        this.sequenceCounter = 0;
    }

    public void setSequence(int[] sequence) {
        this.sequence = sequence;
        this.sequenceCounter = sequenceCounter % sequence.length;
    }

    public void step() {
        this.sequenceCounter = (sequenceCounter + 1) % sequence.length;
    }

    public int output() {
        return sequence[sequenceCounter];
    }

    public void reset() {
        this.sequenceCounter = 0;
    }
}
