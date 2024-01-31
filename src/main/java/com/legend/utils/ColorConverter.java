package com.legend.utils;

/**
 * 系统调色板
 */
public class ColorConverter {

    public static final int[] COLOR_MAP = {
            color(84, 84, 84),
            color(0, 30, 116),
            color(8, 16, 144),
            color(48, 0, 136),
            color(68, 0, 100),
            color(92, 0, 48),
            color(84, 4, 0),
            color(60, 24, 0),
            color(32, 42, 0),
            color(8, 58, 0),
            color(0, 64, 0),
            color(0, 60, 0),
            color(0, 50, 60),
            color(0, 0, 0),
            color(0, 0, 0),
            color(0, 0, 0),
            color(152, 150, 152),
            color(8, 76, 196),
            color(48, 50, 236),
            color(92, 30, 228),
            color(136, 20, 176),
            color(160, 20, 100),
            color(152, 34, 32),
            color(120, 60, 0),
            color(84, 90, 0),
            color(40, 114, 0),
            color(8, 124, 0),
            color(0, 118, 40),
            color(0, 102, 120),
            color(0, 0, 0),
            color(0, 0, 0),
            color(0, 0, 0),
            color(236, 238, 236),
            color(76, 154, 236),
            color(120, 124, 236),
            color(176, 98, 236),
            color(228, 84, 236),
            color(236, 88, 180),
            color(236, 106, 100),
            color(212, 136, 32),
            color(160, 170, 0),
            color(116, 196, 0),
            color(76, 208, 32),
            color(56, 204, 108),
            color(56, 180, 204),
            color(60, 60, 60),
            color(0, 0, 0),
            color(0, 0, 0),
            color(236, 238, 236),
            color(168, 204, 236),
            color(188, 188, 236),
            color(212, 178, 236),
            color(236, 174, 236),
            color(236, 174, 212),
            color(236, 180, 176),
            color(228, 196, 144),
            color(204, 210, 120),
            color(180, 222, 120),
            color(168, 226, 144),
            color(152, 226, 180),
            color(160, 214, 228),
            color(160, 162, 160),
            color(0, 0, 0),
            color(0, 0, 0)
    };

    private static int color(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

}
