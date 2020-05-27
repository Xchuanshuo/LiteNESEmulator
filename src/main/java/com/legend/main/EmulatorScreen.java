package com.legend.main;

import com.legend.screen.DefaultScreen;
import com.legend.screen.Screen;

import javax.swing.*;
import java.awt.*;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description
 */
public class EmulatorScreen extends JComponent {

    private Screen screen = new DefaultScreen();

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (screen != null) {
            g.drawImage(screen.getImage(), 0, 0, getWidth(), getHeight(), null);
        }
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void reset() {
        screen.reset();
        repaint();
    }
}
