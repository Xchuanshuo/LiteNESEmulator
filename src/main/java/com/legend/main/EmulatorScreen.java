package com.legend.main;

import com.legend.screen.DefaultScreen;
import com.legend.screen.Screen;
import com.legend.utils.ImageUtils;
import com.legend.utils.XBRZ;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description
 */
public class EmulatorScreen extends JComponent {

    private Screen screen = new DefaultScreen();
    private XBRZ.ScaleSize scaleSize = XBRZ.ScaleSize.Times2;

    @Override
    public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (screen != null) {
            BufferedImage image = screen.getImage();
            if (scaleSize != null) {
                image = ImageUtils.getEnhanceImage(image, scaleSize);
            }
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void setScaleSize(XBRZ.ScaleSize scaleSize) {
        this.scaleSize = scaleSize;
    }

    public XBRZ.ScaleSize getScaleSize() {
        return scaleSize;
    }

    public void reset() {
        screen.reset();
        repaint();
    }
}
