package com.legend.main.tools;

import com.legend.apu.IAPU;
import com.legend.cpu.CPURegister;
import com.legend.cpu.ICPU;
import com.legend.main.GameRunner;
import com.legend.memory.IMemory;
import com.legend.memory.StandardMemory;
import com.legend.ppu.IPPU;
import com.legend.ppu.PPURegister;
import com.legend.utils.Constants;
import com.legend.utils.StringUtils;
import com.legend.utils.disassemble.DisAssembler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.legend.cpu.StandardCPU.VECTOR_IRQ_OR_BRK;
import static com.legend.cpu.StandardCPU.VECTOR_NMI;
import static com.legend.cpu.StandardCPU.VECTOR_RESET;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;
import static com.legend.utils.Constants.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;

/**
 * @author Legend
 * @data by on 20-5-18.
 * @description 6502 debugger
 */
public class Debugger extends JFrame {

    private Font font = new Font("宋体", Font.PLAIN, 16);
    private GameRunner gameRunner;
    private int instructionLineHeight = 16;

    public Debugger(GameRunner runner) {
        this.gameRunner = runner;
        initView();
        initListener();
    }

    private void initView() {
        setTitle("LiteNES Debugger");
        setResizable(false);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(SCREEN_WIDTH * 2 + 70, SCREEN_WIDTH * 2 - 76));
        JPanel btnPanel = getBtnPanel();
        btnPanel.setVisible(true);

        JScrollPane scrollPane = new JScrollPane(debugPanel);
        scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.NORTH);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private JPanel getBtnPanel() {
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout());

        JButton runBtn = new JButton(RUN);
        runBtn.setBackground(Color.RED);
        JButton stepIntoBtn = new JButton(Constants.STEP_INTO);
        JButton clearAllBreakPointersBtn = new JButton(Constants.CLEAR_BREAK_POINTERS);
        JButton addBreakPointersBtn = new JButton(Constants.ADD_BREAK_POINTER);
        JButton dumpBtn = new JButton(Constants.DUMP);

        runBtn.addActionListener(listener);
        stepIntoBtn.addActionListener(listener);
        clearAllBreakPointersBtn.addActionListener(listener);
        addBreakPointersBtn.addActionListener(listener);
        dumpBtn.addActionListener(listener);
        btnPanel.add(runBtn);
        btnPanel.add(stepIntoBtn);
        btnPanel.add(clearAllBreakPointersBtn);
        btnPanel.add(addBreakPointersBtn);
        btnPanel.add(dumpBtn);
        return btnPanel;
    }

    private ActionListener listener = e -> {
        switch (e.getActionCommand()) {
            case RUN:
                run();
                break;
            case STEP_INTO:
                stepInto();
                break;
            case CLEAR_BREAK_POINTERS:
                clearAllBreakPointers();
                break;
            case ADD_BREAK_POINTER:
                String addressStr = JOptionPane.showInputDialog("请输入地址:", "8000");
                if (StringUtils.isHexNumeric(addressStr)) {
                    int address = Integer.parseInt(addressStr, 16);
                    addBreakPointer(address);
                }
                break;
            case DUMP:
                dumpMemoryProgram();
                break;
            default: break;
        }
    };

    private void initListener() {
        debugPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getButton()) {
                    case MouseEvent.BUTTON3:
                        int line = e.getY() / instructionLineHeight;
                        List<String> instructionList = DisAssembler.dumpByCount(gameRunner.getCPU().getMemory(),
                                gameRunner.getCPU().getRegister().getPC(), 30);
                        if (line >= instructionList.size()) return;
                        int address = getAddress(instructionList.get(line));
                        addBreakPointer(address);
                        break;
                }
            }
        });
    }

    private void run() {
        gameRunner.setStepInto(false);
        gameRunner.resume();
        debugPanel.repaint();
    }

    private void stepInto() {
        gameRunner.setStepInto(true);
        gameRunner.resume();
        debugPanel.repaint();
    }

    private void addBreakPointer(int address) {
        Set<Integer> breakPointers = gameRunner.getBreakpointers();
        if (breakPointers.contains(address)) {
            breakPointers.remove(address);
        } else {
            breakPointers.add(address);
            System.out.println("新增了断点: " + String.format("%04X", address));
        }
        debugPanel.repaint();
    }

    private void dumpMemoryProgram() {
        gameRunner.pause();
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("program-dump.txt");
            StandardMemory memory = (StandardMemory) gameRunner.getCPU().getMemory();
            DisAssembler.dump(memory, 0x8000, 0xFFFE, outputStream);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        System.out.println("dump program 成功!");
        gameRunner.resume();
    }

    private void clearAllBreakPointers() {
        gameRunner.getBreakpointers().clear();
        debugPanel.repaint();
        System.out.println("清空所有断点");
    }

    private JPanel debugPanel = new JPanel() {

        @Override
        public void paint(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics;
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (gameRunner.isStop()) return;

            ICPU cpu = gameRunner.getCPU();
            g.setColor(Color.WHITE);
            g.setFont(font);

            drawInstruction(g, cpu);

            drawStatus(g, cpu);
        }

        private void drawInstruction(Graphics2D g, ICPU cpu) {
            g.setColor(Color.PINK);
            g.fillRect(0, 0, SCREEN_WIDTH, instructionLineHeight + 2);
            g.setColor(Color.BLACK);

            Set<Integer> breakPointers = gameRunner.getBreakpointers();
            List<String> instructionList = DisAssembler.dumpByCount(cpu.getMemory(), cpu.getRegister().getPC(), 30);
            for (int i = 0;i < instructionList.size();i++) {
                int address = getAddress(instructionList.get(i).trim());
                if (breakPointers.contains(address)) {
                    g.setColor(Color.red);
                    g.fillRect(0, i * instructionLineHeight, SCREEN_WIDTH, instructionLineHeight + 2);
                    g.setColor(Color.BLACK);
                }
                g.drawString(instructionList.get(i), 0, instructionLineHeight * i + instructionLineHeight);
            }
        }

        private void drawStatus(Graphics2D g, ICPU cpu) {
            int xOffset = 24;

            drawCPUStatus(g, gameRunner.getCPU(), xOffset);
            drawPPUStatus(g, gameRunner.getPPU(), SCREEN_WIDTH + xOffset, 160);
            drawAPUStatus(g, gameRunner.getAPU(), 0, 140);

            IMemory mainMemory = cpu.getMemory();
            g.translate(0, 40);
            g.drawString("Vectors: NMI   RESET  IRQ/BRK", 0, 0);
            g.drawString(String.format("         %04X   %04X    %04X",
                    mainMemory.readByte(VECTOR_NMI[0]) | (mainMemory.readByte(VECTOR_NMI[1]) << 8),
                    mainMemory.readByte(VECTOR_RESET[0]) | (mainMemory.readByte(VECTOR_RESET[1]) << 8),
                    mainMemory.readByte(VECTOR_IRQ_OR_BRK[0]) | (mainMemory.readByte(VECTOR_IRQ_OR_BRK[1]) << 8)
            ), 0, 20);
        }

        private void drawCPUStatus(Graphics2D g, ICPU cpu, int xOffset) {
            CPURegister cpuRegister = cpu.getRegister();
            g.drawString(String.format("A: %02X", cpuRegister.getA()), SCREEN_WIDTH + xOffset, 12);
            g.drawString(String.format("X: %02X", cpuRegister.getX()), SCREEN_WIDTH  + xOffset, 27);
            g.drawString(String.format("Y: %02X", cpuRegister.getY()), SCREEN_WIDTH + xOffset + 60, 27);
            g.drawString(String.format("SP: %04X", cpuRegister.getSP()), SCREEN_WIDTH + xOffset, 42);
            g.drawString(String.format("PC: %04X", cpuRegister.getPC()), SCREEN_WIDTH + xOffset, 57);
            g.drawString("       N V B C I Z C", SCREEN_WIDTH + xOffset, 87);
            g.drawString(String.format("Flags: %1s %1s %1s %1s %1s %1s %1s",
                    cpuRegister.isNegative() ? "1" : "0",
                    cpuRegister.isOverflow() ? "1" : "0",
                    cpuRegister.isBreak() ? "1" : "0",
                    cpuRegister.isDecimal() ? "1" : "0",
                    cpuRegister.isDisableInterrupt() ? "1" : "0",
                    cpuRegister.isZero() ? "1" : "0",
                    cpuRegister.isCarry() ? "1" : "0"), SCREEN_WIDTH + xOffset, 105);

            g.drawString(String.format("Stack: %02X %02X %02X %02X",
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 1),
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 2),
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 3),
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 4)),
                    SCREEN_WIDTH + xOffset, 125);
            g.drawString(String.format("       %02X %02X %02X %02X",
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 5),
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 6),
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 7),
                    cpu.getMemory().readByte(0x100 + cpuRegister.getSP() + 8)),
                    SCREEN_WIDTH + xOffset, 138);
        }

        private void drawPPUStatus(Graphics2D g, IPPU ppu, int xOffset, int yOffset) {
            g.translate(xOffset, yOffset);
            PPURegister pr = ppu.getRegister();
            g.drawString(String.format("PPU Control1: %08d", Integer.parseInt(Integer.toString(pr.readByte(0), 2))), 0, 10);
            g.drawString(String.format("PPU Control2: %08d", Integer.parseInt(Integer.toString(pr.readByte(1), 2))), 0, 25);
            g.drawString(String.format("XScroll: %d", pr.getXScrollConsiderBaseNameTableAddress()),  0, 40);
            g.drawString(String.format("YScroll: %d", pr.getYScrollConsiderBaseNameTableAddress()), 0, 55);
            g.drawString(String.format("PPU Status: %08d", Integer.parseInt(Integer.toString(pr.getData(2), 2))), 0, 70);
            g.drawString(String.format("PPU Address: %04X", pr.getPPUAddress()), 0, 85);

            g.drawString(String.format("Scanline: %d", ppu.getScanline()), 0, 110);
            g.drawString(String.format("Cycle: %d", ppu.getCycle()), 0, 125);
        }

        private void drawAPUStatus(Graphics2D g, IAPU apu, int xOffset, int yOffset) {
            g.translate(xOffset, yOffset);
            g.drawString(String.format("APU Interrupt: %s", String.valueOf(!apu.getRegister().isInterruptDisable())), 0, 12);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(SCREEN_WIDTH * 2 + 70,SCREEN_WIDTH * 2);
        }
    };

    private int getAddress(String str) {
        return Integer.parseInt(str.substring(2, 6).trim(), 16);
    }

    public static void main(String[] args) {
        String str = "0x8000: SEI             78\n";
        System.out.println(Integer.parseInt(str.substring(2, 6), 16));
        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
        System.out.println(Arrays.toString(fonts));
    }

}
