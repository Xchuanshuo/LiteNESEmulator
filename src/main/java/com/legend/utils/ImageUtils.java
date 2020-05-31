package com.legend.utils;

import java.awt.image.BufferedImage;

/**
 * @author Legend
 * @data by on 20-5-31.
 * @description
 */
public class ImageUtils {

    private static XBRZ xbrz = new XBRZ();

    public static BufferedImage getEnhanceImage(BufferedImage srcImg, XBRZ.ScaleSize scaleSize) {
        int width = srcImg.getWidth();
        int height = srcImg.getHeight();
        int[] inputPixels = srcImg.getRGB(0, 0, width, height, null, 0, width);
        int[] targetPixels = new int[width * height * scaleSize.size * scaleSize.size];
        xbrz.scaleImage(scaleSize, inputPixels, targetPixels,
                        width, height, new XBRZ.ScalerCfg(),
                    0, height);
        return getImageByPixels(targetPixels, width * scaleSize.size, height * scaleSize.size);
    }

    public static BufferedImage getImageByPixels(int[] pixels, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }
}
