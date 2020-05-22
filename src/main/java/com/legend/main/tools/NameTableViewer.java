package com.legend.main.tools;

import com.legend.main.GameRunner;
import com.legend.memory.IMemory;
import com.legend.memory.StandardMemory;
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

import static com.legend.ppu.IPPU.SCREEN_HEIGHT;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;

/**
 * @author Legend
 * @data by on 20-5-19.
 * @description 名称表可视化
 */
public class NameTableViewer extends JFrame {

    private GameRunner gameRunner;
    private JLabel mirroringTypeLabel = new JLabel();
    private LookMemoryFrame lookMemoryFrame;

    public NameTableViewer(GameRunner gameRunner) {
        this.gameRunner = gameRunner;
        initView();
    }

    private void initView() {
        setTitle("NameTable Viewer");
        setResizable(false);
        setLayout(new BorderLayout());
        JPanel topPanel = getTopPanel();
        topPanel.setVisible(true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setPreferredSize(new Dimension(SCREEN_WIDTH * 2,
                SCREEN_HEIGHT * 2 + 1));
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(1);
        splitPane.setTopComponent(nameTableUpPanel);
        splitPane.setBottomComponent(nameTableDownPanel);
        splitPane.setDividerLocation(SCREEN_HEIGHT + 1);

        add(splitPane, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private JPanel getTopPanel() {
        mirroringTypeLabel.setText("MirroringType: " + gameRunner.getPPU().getMirroringType());
        JPanel topPanel = new JPanel();
        JButton dumpBtn = new JButton(Constants.DUMP);
        JButton lookMemoryBtn = new JButton(Constants.LOOK_MEMORY);
        dumpBtn.addActionListener(e -> dumpNameTableMemory());
        lookMemoryBtn.addActionListener(e -> {
            if (lookMemoryFrame == null) {
                lookMemoryFrame = new LookMemoryFrame(gameRunner.getPPU().getVRAM()
                        ,"NameTable Memory",0x2000, 0x3000);
            } else {
                lookMemoryFrame.setVisible(true);
            }
        });

        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setHgap(20);
        topPanel.setLayout(flowLayout);
        topPanel.add(lookMemoryBtn);
        topPanel.add(dumpBtn);
        topPanel.add(mirroringTypeLabel);
        return topPanel;
    }

    private void dumpNameTableMemory() {
        gameRunner.pause();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("ppu-name-table-dump.txt");
            StandardMemory memory = gameRunner.getPPU().getVRAM();
            DisAssembler.dumpMemoryNativeData(memory, 0x2000, 0x3000, outputStream);
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
        System.out.println("dump PPU NameTable 成功!");
        gameRunner.resume();
    }

    private JPanel nameTableUpPanel = new JPanel() {

        @Override
        public void paint(Graphics g) {
            IPPU ppu = gameRunner.getPPU();
            Screen screen = new DefaultScreen();
            drawNameTable(g,  ppu,0x2000, screen);
            g.translate(SCREEN_WIDTH, 0);
            drawNameTable(g, ppu, 0x2400, screen);
        }
    };

    private JPanel nameTableDownPanel = new JPanel() {

        @Override
        public void paint(Graphics g) {
            IPPU ppu = gameRunner.getPPU();
            Screen screen = new DefaultScreen();
            drawNameTable(g,  ppu,0x2800, screen);
            g.translate(SCREEN_WIDTH, 0);
            drawNameTable(g, ppu, 0x2C00, screen);
        }

    };

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        mirroringTypeLabel.setText("MirroringType: " + gameRunner.getPPU().getMirroringType());
        if (lookMemoryFrame != null) {
            lookMemoryFrame.repaint();
        }
    }

    private void drawNameTable(Graphics g, IPPU ppu, int startAddress, Screen screen) {
        IMemory vram = ppu.getVRAM();
        PPURegister ppuRegister = ppu.getRegister();
        int renderX = 0, renderY = 0;
        int offset = 0;
        // 名称表都是1024字节(960字节背景图案+64字节资源)
        int endAddress = startAddress | 0x3C0;
        for (int i = startAddress;i < endAddress;i++) {
            int tileNumber = vram.readByte(i);
            int patternAddress = ppuRegister.getBackgroundPatternTableAddress() + tileNumber * 16;
            int attributeAddress = endAddress + offset + renderX / 32;
            int attribute = vram.readByte(attributeAddress);
            boolean isLeft = (renderX / 16) % 2 == 0;
            boolean isTop = (renderY / 16) % 2 == 0;
            int high2Bit;
            if (isLeft && isTop) {
                high2Bit = ByteUtils.getBitsByRange(attribute, 0, 1);
            } else if (isTop) {
                high2Bit = ByteUtils.getBitsByRange(attribute, 2, 3);
            } else if (isLeft) {
                high2Bit = ByteUtils.getBitsByRange(attribute, 4, 5);
            } else {
                high2Bit = ByteUtils.getBitsByRange(attribute, 6, 7);
            }
            int y = renderY;
            for (int j = patternAddress;j < patternAddress + 8;j++) {
                int x = renderX;
                int low = vram.readByte(j);
                int high = vram.readByte(j + 8);
                for (int k = 7; k >= 0; k--) {
                    int low2Bit = ByteUtils.getBit(low, k) |
                            ByteUtils.getBit(high, k) << 1;
                    screen.set(x++, y, ppu.getPalette().readByte(high2Bit << 2 | low2Bit));
                }
                y++;
            }

            renderX += 8;
            if (renderX == 8 * 32) {
                renderX = 0;
                renderY += 8;
                // 确定attribute的偏移位置
                if (renderY % 32 == 0) {
                    offset += 8;
                }
            }
        }
        if (ppu.getRegister().showSprites() && ppu.getRegister().showBackground()) {
            g.drawImage(screen.getImage(), 0, 0, screen.getWidth(),
                    screen.getHeight(), null);

        }
    }
}
