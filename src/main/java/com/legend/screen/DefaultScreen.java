package com.legend.screen;

import com.legend.utils.ColorConverter;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import static com.legend.ppu.IPPU.SCREEN_HEIGHT;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description 使用颜色map来映射到对应的颜色, 并转换为BufferedImage
 */
public class DefaultScreen implements Screen {

    private BufferedImage image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
    private int[] imageBuffer = new int[SCREEN_WIDTH * SCREEN_HEIGHT];
    private byte[] colorBuffer = new byte[SCREEN_WIDTH * SCREEN_HEIGHT];

    public DefaultScreen() {
        Arrays.fill(colorBuffer, (byte) 0x3F);
    }

    @Override
    public void set(int x, int y, int colorIndex) {
        // 存储每个坐标对应的调色板索引
        colorBuffer[SCREEN_WIDTH * y + x] = (byte) colorIndex;
    }

    @Override
    public int getWidth() {
        return SCREEN_WIDTH;
    }

    @Override
    public int getHeight() {
        return SCREEN_HEIGHT;
    }

    @Override
    public BufferedImage getImage() {
        for (int i = 0;i < colorBuffer.length;i++) {
            imageBuffer[i] = ColorConverter.COLOR_MAP[colorBuffer[i] & 0x3F];
        }
        image.setRGB(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, imageBuffer, 0, SCREEN_WIDTH);
        return image;
    }
}
