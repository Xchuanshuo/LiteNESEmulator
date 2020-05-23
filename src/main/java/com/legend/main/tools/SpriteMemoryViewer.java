package com.legend.main.tools;

import com.legend.main.GameRunner;
import com.legend.memory.IMemory;
import com.legend.ppu.IPPU;
import com.legend.ppu.PPURegister;
import com.legend.screen.DefaultScreen;
import com.legend.screen.Screen;
import com.legend.utils.ByteUtils;
import com.legend.utils.Constants;
import com.legend.utils.disassemble.DisAssembler;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.legend.ppu.IPPU.SCREEN_HEIGHT;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;

/**
 * @author Legend
 * @data by on 20-5-20.
 * @description
 */
public class SpriteMemoryViewer extends JFrame {

    private Font font = new Font("宋体", Font.PLAIN, 16);
    private GameRunner gameRunner;

    public SpriteMemoryViewer(GameRunner gameRunner) {
        this.gameRunner = gameRunner;
        initView();
    }

    private void initView() {
        setTitle("Sprite Memory");
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        JButton dumpBtn = new JButton(Constants.DUMP);
        dumpBtn.addActionListener(e -> dumpSprMemory());
        topPanel.setLayout(new FlowLayout());
        topPanel.add(dumpBtn);
        topPanel.setVisible(true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setPreferredSize(new Dimension(SCREEN_WIDTH * 2 + 20,
                SCREEN_HEIGHT + 50));
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(1);
        splitPane.setLeftComponent(spriteViewerPanel);
        splitPane.setRightComponent(new JScrollPane(spriteMemoryPanel));
        splitPane.setDividerLocation(SCREEN_WIDTH);
        add(splitPane, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void dumpSprMemory() {
        gameRunner.pause();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("ppu-sprite-memory-dump.txt");
            IMemory memory = gameRunner.getPPU().getSprRAM();
            DisAssembler.dumpMemoryNativeData(memory, 8,0, 256, outputStream);
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
        System.out.println("dump Sprite Memory 成功!");
        gameRunner.resume();
    }

    private JPanel spriteViewerPanel = new JPanel() {

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(font);
            g.setColor(Color.ORANGE);
            Screen screen = new DefaultScreen();
            IPPU ppu = gameRunner.getPPU();
            PPURegister ppuRegister = ppu.getRegister();
            IMemory sprMemory = ppu.getSprRAM();

//            int h = ppu.getRegister().is8x16() ? 16 : 8;
//            for (int i = 0;i < 256;i += 4) {
//                int y = sprMemory.readByte(i) + 1;
//                int x = sprMemory.readByte(i + 3);
//                g.drawRect(x, y, 8, h);
//            }
            for (int i = 0;i < 256;i += 4) {
                int y = sprMemory.readByte(i) + 1;
                int tileNumber = sprMemory.readByte(i + 1);
                int high2Bit = (sprMemory.readByte(i + 2) & 3) << 2;
                int x = sprMemory.readByte(i + 3);
                int patternAddress = ppuRegister.getBackgroundPatternTableAddress()+ tileNumber * 16;
                renderSprite(ppu, screen, x, y, patternAddress, 0x10 + high2Bit);
                if (ppu.getRegister().is8x16()) {
                    renderSprite(ppu, screen, x, y + 8, patternAddress, 0x10 + high2Bit);
                }
//                g.drawRect(x, y, 8, h);
            }
            g.drawImage(screen.getImage(), 0, 0, screen.getWidth(), screen.getHeight(), null);
        }

        private void renderSprite(IPPU ppu, Screen screen,
                                  int renderX, int renderY,
                                  int patternAddress, int high2Bit) {
            IMemory memory = ppu.getVRAM();
            int y = renderY;
            for (int i = patternAddress;i < patternAddress + 8;i++) {
                int x = renderX;
                int low = memory.readByte(i);
                int high = memory.readByte(i + 8);
                for (int k = 7;k >= 0; k--) {
                    int val = ByteUtils.getBit(low, k) |
                            ByteUtils.getBit(high, k) << 1;
                    if (x < SCREEN_WIDTH && y < SCREEN_HEIGHT) {
                        screen.set(x++, y, ppu.getPalette().readByte(high2Bit | val));
                    }
                }
                y++;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT * 2 + 30);
        }
    };

    private JPanel spriteMemoryPanel = new JPanel() {

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(font);
            g.setColor(Color.BLACK);

            g.translate(0, 16);
            IMemory sprMemory = gameRunner.getPPU().getSprRAM();
            List<String> dataList = DisAssembler.getMemoryNativeData(sprMemory,
                    8, 0, 256);
            for (int i = 0;i < dataList.size();i++) {
                g.drawString(dataList.get(i), 0, 16 * i);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT * 2 + 50);
        }
    };
}
