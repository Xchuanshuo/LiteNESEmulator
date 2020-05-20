package com.legend.apu;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-5-1.
 * @description
 */
public interface DividerListener extends Serializable {
    void onClock(Divider divider);
}
