package com.legend.screen;

import java.awt.image.BufferedImage;

/**
 * @author Legend
 * @data by on 20-4-12.
 * @description 屏幕接口
 */
public interface Screen {

    void set(int x, int y, int colorIndex);
    int getWidth();
    int getHeight();
    BufferedImage getImage();
}
