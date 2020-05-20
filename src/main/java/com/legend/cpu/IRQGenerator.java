package com.legend.cpu;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description
 */
public interface IRQGenerator extends Serializable {

    boolean getIRQLevel();
}
