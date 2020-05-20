package com.legend.main.tools;

import com.legend.main.GameRunner;
import com.legend.memory.IMemory;
import com.legend.memory.StandardMemory;
import com.legend.ppu.IPPU;
import com.legend.ppu.PPURegister;
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
        JPanel btnPanel = new JPanel();
        JButton dumpBtn = new JButton(Constants.DUMP);
        dumpBtn.addActionListener(e -> dumpSprMemory());
        btnPanel.setLayout(new FlowLayout());
        btnPanel.add(dumpBtn);
        btnPanel.setVisible(true);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setPreferredSize(new Dimension(SCREEN_WIDTH * 2 + 20,
                SCREEN_HEIGHT + 50));
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(1);
        splitPane.setLeftComponent(spriteViewerPanel);
        splitPane.setRightComponent(new JScrollPane(spriteMemoryPanel));
        splitPane.setDividerLocation(SCREEN_WIDTH);
        add(splitPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.NORTH);

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
            DisAssembler.dumpMemoryNativeData(memory, 0, 256, outputStream);
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

            IPPU ppu = gameRunner.getPPU();
            IMemory sprMemory = ppu.getSprRAM();
            int h = ppu.getRegister().is8x16() ? 16 : 8;
            for (int i = 0; i<256;i += 4) {
                int y = sprMemory.readByte(i) + 1;
                int x = sprMemory.readByte(i + 3);
                g.drawRect(x, y, 8, h);
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

            IMemory sprMemory = gameRunner.getPPU().getSprRAM();
            List<String> dataList = DisAssembler.getMemoryNativeData(sprMemory,
                    8, 0, 256);
            for (int i = 0;i < dataList.size();i++) {
                g.drawString(dataList.get(i), 0, 16 * i);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT * 2 + 30);
        }
    };
}
