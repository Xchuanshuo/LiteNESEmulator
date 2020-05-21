package com.legend.main.tools;

import com.legend.main.GameRunner;
import com.legend.memory.IMemory;
import com.legend.memory.StandardMemory;
import com.legend.ppu.IPPU;
import com.legend.screen.Screen;
import com.legend.utils.ByteUtils;
import com.legend.utils.ColorConverter;
import com.legend.utils.Constants;
import com.legend.utils.disassemble.DisAssembler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.legend.ppu.IPPU.SCREEN_WIDTH;

/**
 * @author Legend
 * @data by on 20-5-19.
 * @description 图案表可视化
 */
public class PatternTableViewer extends JFrame {

    private GameRunner gameRunner;

    public PatternTableViewer(GameRunner runner) {
        this.gameRunner = runner;
        initView();
    }

    private void initView() {
        setTitle("PatternTable Viewer");
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        JButton dumpBtn = new JButton(Constants.DUMP);
        dumpBtn.addActionListener(e -> dumpPPUPatternTable());
        topPanel.setLayout(new FlowLayout());
        topPanel.add(dumpBtn);
        topPanel.setVisible(true);

        add(patternTablePanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void dumpPPUPatternTable() {
        gameRunner.pause();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("ppu-pattern-table-dump.txt");
            StandardMemory memory = gameRunner.getPPU().getVRAM();
            DisAssembler.dumpMemoryNativeData(memory, 0, 0x2000, outputStream);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("dump PPU VRAM 成功!");
        gameRunner.resume();
    }

    private JPanel patternTablePanel = new JPanel() {
        {
            setResizable(false);
            setPreferredSize(new Dimension(SCREEN_WIDTH * 2 + 70, SCREEN_WIDTH * 2 - 120));
        }

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics;
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());

            IPPU ppu = gameRunner.getPPU();
            g.translate(2, 0);
            g.scale(2.2, 2.2);
            drawCHR(g, ppu, 0, 0x1000);
            g.translate(135, 0);
            drawCHR(g, ppu, 0x1000, 0x2000);

            g.translate(-132, 140);
            drawPalette(g, ppu.getPalette());
        }

        private void drawCHR(Graphics2D g, IPPU ppu, int startAddress, int endAddress) {
            StandardMemory memory = ppu.getVRAM();
            Screen screen = new PatternTableScreen();
            int renderY = 0, renderX = 0;
            for (int i = startAddress;i < endAddress;i += 16) {
                int y = renderY;
                for (int j = i;j < i + 8;j++) {
                    int x = renderX;
                    int low = memory.readByte(j);
                    int high = memory.readByte(j + 8);
                    for (int k = 7; k >= 0; k--) {
                        int val = ByteUtils.getBit(low, k) |
                                ByteUtils.getBit(high, k) << 1;
                        screen.set(x++, y, ppu.getPalette().readByte(val));
                    }
                    y++;
                }
                renderX += 8;
                // 一行绘制16个8*8的像素块
                if (renderX == 8 * 16) {
                    renderX = 0;
                    renderY += 8;
                }
            }
            g.drawImage(screen.getImage(), 0, 0, screen.getWidth(),
                    screen.getHeight(), null);
        }

        private void drawPalette(Graphics2D g, IMemory paletteMemory) {
            int width = 16, height = 16;
            int renderX = 0, renderY = 0;
            for (int i = 0;i < 32;i++) {
                int color = ColorConverter.COLOR_MAP[paletteMemory.readByte(i) & 0x3F];
                g.setColor(new Color(color));
                g.fillRect(renderX, renderY, width, height);
                renderX += width;
                if (renderX == width * 16) {
                    renderX = 0;
                    renderY += height + 2;
                }
            }
        }
    };

    class PatternTableScreen implements Screen {

        private int width = 128, height = 128;
        private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        private int[] imageBuffer = new int[width * height];
        private byte[] colorBuffer = new byte[width * height];

        public PatternTableScreen() {
            Arrays.fill(colorBuffer, (byte) 0x3F);
        }

        @Override
        public void set(int x, int y, int colorIndex) {
            // 存储每个坐标对应的调色板索引
            colorBuffer[width * y + x] = (byte) colorIndex;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public BufferedImage getImage() {
            for (int i = 0;i < colorBuffer.length;i++) {
                imageBuffer[i] = ColorConverter.COLOR_MAP[colorBuffer[i] & 0x3F];
            }
            image.setRGB(0, 0, width, height, imageBuffer, 0, width);
            return image;
        }
    }
}
