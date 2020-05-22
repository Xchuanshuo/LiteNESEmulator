package com.legend.main.tools;

import com.legend.main.GameRunner;
import com.legend.memory.IMemory;
import com.legend.utils.StringUtils;
import com.legend.utils.disassemble.DisAssembler;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.legend.ppu.IPPU.SCREEN_HEIGHT;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;

/**
 * @author Legend
 * @data by on 20-5-21.
 * @description 查看内存十六进制值
 */
public class LookMemoryFrame extends JFrame {

    private Font font = new Font("宋体", Font.PLAIN, 16);
    private IMemory memory;
    private int startAddress, endAddress;
    private String title;

    public LookMemoryFrame(IMemory memory,int startAddress, int endAddress) {
        this(memory, "", startAddress, endAddress);
    }

    public LookMemoryFrame(IMemory memory, String title, int startAddress, int endAddress) {
        this.memory = memory;
        this.title = title;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        initView();
    }

    private void initView() {
        if (!StringUtils.isEmpty(title)) {
            setTitle(title);
        } else {
            setTitle("Memory Watcher");
        }
        setResizable(true);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(SCREEN_WIDTH * 2,
                SCREEN_HEIGHT * 2));
        add(new JScrollPane(memoryPanel), BorderLayout.CENTER);

        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private JPanel memoryPanel = new JPanel() {

        private int height = SCREEN_HEIGHT * 2;

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(font);
            g.setColor(Color.BLACK);

            List<String> dataList = DisAssembler.getMemoryNativeData(memory,
                    16, startAddress, endAddress);
            g.translate(0, 16);
            for (int i = 0;i < dataList.size();i++) {
                g.drawString(dataList.get(i), 0, 16 * i);
            }
            this.height = (dataList.size() + 1) * 16;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(SCREEN_WIDTH, height);
        }
    };

}
