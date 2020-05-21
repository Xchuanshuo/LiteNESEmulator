package com.legend.main;

import com.legend.input.StandardControllers;
import com.legend.main.tools.Debugger;
import com.legend.main.tools.NameTableViewer;
import com.legend.main.tools.PatternTableViewer;
import com.legend.main.tools.SpriteMemoryViewer;
import com.legend.memory.StandardMemory;
import com.legend.storage.LocalStorage;
import com.legend.utils.Constants;
import com.legend.utils.disassemble.DisAssembler;
import com.legend.utils.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.legend.ppu.IPPU.SCREEN_HEIGHT;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;
import static com.legend.utils.Constants.*;
import static com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl.DEBUG;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description
 */
public class Emulator extends JFrame implements Runnable, KeyListener {

    public static double CPU_CYCLE_PER_SECOND = 1789772.5;
    public static int SPEAKER_SAMPLE_RATE = 44100;

    private GameRunner gameRunner;
    private StandardControllers controllers = new StandardControllers();
    private EmulatorScreen emulatorScreen = new EmulatorScreen();
    private EmulatorSpeaker emulatorSpeaker = new EmulatorSpeaker();
    private Map<Integer, Integer> keyBindings = new HashMap<>();

    private boolean isFullScreen = false;
    private GraphicsDevice gd;

    private LocalStorage storage = new LocalStorage();

    private Debugger debugger;
    private SpriteMemoryViewer spriteMemoryViewer;
    private PatternTableViewer patternTableViewer;
    private NameTableViewer nameTableViewer;

    public Emulator() {
        SwingUtilities.invokeLater(this::initFrame);
        // P1
        keyBindings.put(KeyEvent.VK_W, StandardControllers.P1_KEY_UP);
        keyBindings.put(KeyEvent.VK_S, StandardControllers.P1_KEY_DOWN);
        keyBindings.put(KeyEvent.VK_A, StandardControllers.P1_KEY_LEFT);
        keyBindings.put(KeyEvent.VK_D, StandardControllers.P1_KEY_RIGHT);
        keyBindings.put(KeyEvent.VK_J, StandardControllers.P1_KEY_A);
        keyBindings.put(KeyEvent.VK_K, StandardControllers.P1_KEY_B);
        keyBindings.put(KeyEvent.VK_SPACE, StandardControllers.P1_KEY_SELECT);
        keyBindings.put(KeyEvent.VK_ENTER, StandardControllers.P1_KEY_START);
        // P2
        keyBindings.put(KeyEvent.VK_UP, StandardControllers.P2_KEY_UP);
        keyBindings.put(KeyEvent.VK_DOWN, StandardControllers.P2_KEY_DOWN);
        keyBindings.put(KeyEvent.VK_LEFT, StandardControllers.P2_KEY_LEFT);
        keyBindings.put(KeyEvent.VK_RIGHT, StandardControllers.P2_KEY_RIGHT);
        keyBindings.put(KeyEvent.VK_NUMPAD1, StandardControllers.P2_KEY_A);
        keyBindings.put(KeyEvent.VK_NUMPAD2, StandardControllers.P2_KEY_B);
    }

    public void startGame(String gamePath) {
        storage.setPath(StringUtils.getSaveName(gamePath) + ".sl");
        try {
            gameRunner = new GameRunner(gamePath, controllers, emulatorScreen.getScreen(),
                    emulatorSpeaker.getSpeaker(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(emulatorSpeaker).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(gameRunner).start();
//        gameRunner.pause();
    }

    private void initFrame() {
        emulatorScreen.setPreferredSize(new Dimension(SCREEN_WIDTH * 3, SCREEN_HEIGHT * 3));
        emulatorScreen.setFocusable(false);

        addKeyListener(this);
        initMenuBar();
        setLayout(new BorderLayout());
        add(emulatorScreen);
        pack();
        setTitle("LiteNES Emulator");
//        setIconImage();
        setLocationRelativeTo(null);
    }

    private void initMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();

        JMenu fileMenu = getFileMenu();
        JMenu toolsMenu = getToolsMenu();

        jMenuBar.add(fileMenu);
        jMenuBar.add(toolsMenu);
        setJMenuBar(jMenuBar);
    }

    private JMenu getFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem resetItem = new JMenuItem(RESET);
        JMenuItem loadRomItem = new JMenuItem(LOAD_ROM);
        JMenuItem saveStatusItem = new JMenuItem(SAVE_STATUS);
        JMenuItem loadStatus = new JMenuItem(LOAD_STATUS);
        fileMenu.add(resetItem);
        fileMenu.add(loadRomItem);
        fileMenu.add(saveStatusItem);
        fileMenu.add(loadStatus);
        resetItem.addActionListener(fileListener);
        loadRomItem.addActionListener(fileListener);
        saveStatusItem.addActionListener(fileListener);
        loadStatus.addActionListener(fileListener);
        return fileMenu;
    }

    private JMenu getToolsMenu() {
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem debugItem = new JMenuItem(Constants.DEBUG);
        JMenuItem sprmItem = new JMenuItem(Constants.SPRM);
        JMenuItem patternTableViewerItem = new JMenuItem(PATTERN_TABLES_VIEWER);
        JMenuItem nameTableViewerItem = new JMenuItem(Constants.NAME_TABLES_VIEWER);
        toolsMenu.add(debugItem);
        toolsMenu.add(sprmItem);
        toolsMenu.add(patternTableViewerItem);
        toolsMenu.add(nameTableViewerItem);
        debugItem.addActionListener(toolsListener);
        patternTableViewerItem.addActionListener(toolsListener);
        sprmItem.addActionListener(toolsListener);
        nameTableViewerItem.addActionListener(toolsListener);
        return toolsMenu;
    }

    private ActionListener fileListener = e -> {
        switch (e.getActionCommand()) {
            case RESET:
                break;
            case LOAD_ROM:
                if (gameRunner != null) {
                    gameRunner.stop();
                    System.out.println("load rom");
                }
                break;
            case SAVE_STATUS:
            case LOAD_STATUS:
                showOperation();
                break;
            default: break;
        }
    };

    private ActionListener toolsListener = e -> {
        switch (e.getActionCommand()) {
            case DEBUG:
                if (!gameRunner.isEnableDebug()) {
                    gameRunner.setEnableDebug(true);
                    gameRunner.setStepInto(true);
                    this.debugger = new Debugger(gameRunner);
                    System.out.println("debug");
                }
                break;
            case PATTERN_TABLES_VIEWER:
                patternTableViewer = new PatternTableViewer(gameRunner);
                break;
            case SPRM:
                spriteMemoryViewer = new SpriteMemoryViewer(gameRunner);
                break;
            case NAME_TABLES_VIEWER:
                gameRunner.pause();
                nameTableViewer = new NameTableViewer(gameRunner);
                gameRunner.resume();
                break;
            default: break;
        }
    };

    private void reloadGame(String path) {
        System.out.println("加载游戏存档: " + path);
        if (gameRunner != null) {
            System.out.println("开始加载");
            storage.load(gameRunner);
            // 注意手柄也需要更新为新加载的
            this.controllers = (StandardControllers) ((StandardMemory) gameRunner.getCPU()
                    .getMemory()).getMemory(0x4016);
            System.out.println("加载完成");
        }
    }

    private void showOperation() {
        gameRunner.pause();
        if (gameRunner != null) {
            storage.setPath(gameRunner.getLoader().getFileMD5() + ".sl");
        }
        String[] options = new String[]{"读取存档", "保存"};
        int idx = JOptionPane.showOptionDialog(
                this, "请点击一个按钮选择一项", "提示",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]
        );
        switch (idx) {
            case 0:
                loadStatus();
                break;
            case 1:
                saveStatus();
                break;
            default: break;
        }
        gameRunner.resume();
    }

    private void loadStatus() {
        JFileChooser fc = new JFileChooser();
        // 根据文件名hash值 和 后缀名过滤
        FileNameFilter fileNameFilter = new FileNameFilter("NES save file", "sl");
        fileNameFilter.setName(storage.getPath().substring(0,
                storage.getPath().lastIndexOf(".")));
        fc.setFileFilter(fileNameFilter);
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(new File("."));
        fc.showOpenDialog(this);
        if (fc.getSelectedFile() != null) {
            reloadGame(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void saveStatus() {
        if (gameRunner != null) {
            boolean isSuccess = storage.save(gameRunner);
            if (isSuccess) {
                JOptionPane.showMessageDialog(this, "保存成功!");
            } else {
                System.out.println("保存失败");
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        Integer r = keyBindings.get(e.getKeyCode());
        if (r != null) {
            controllers.press((r / 8) & 1, (r % 8) & 0xFF);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Integer r = keyBindings.get(e.getKeyCode());
        if (r != null) {
            controllers.release((r / 8) & 1, (r % 8) & 0xFF);
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_M:
                showOperation();
                break;
            case KeyEvent.VK_V: // dump vram
                gameRunner.pause();
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream("ppu-vram-dump.txt");
                    StandardMemory memory = gameRunner.getPPU().getVRAM();
                    DisAssembler.dumpMemoryNativeData(memory, 0, 0x4000, outputStream);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                System.out.println("dump PPU VRAM 成功!");
                gameRunner.resume();
                break;
            case KeyEvent.VK_P:
                fullScreen();
                break;
            default: break;
        }
    }

    private void fullScreen() {
        if (isFullScreen) {
            this.dispose();
            setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            setVisible(true);
            isFullScreen = false;
        } else {
            gd = getGraphicsConfiguration().getDevice();
            if (!gd.isFullScreenSupported()) {
                //then fullscreen will give a window the size of the screen instead
                // messageBox("Fullscreen is not supported by your OS or version of Java.");
                return;
            }
            this.dispose();
            this.setUndecorated(true);

            gd.setFullScreenWindow(this);
            this.setVisible(true);

            DisplayMode dm = gd.getDisplayMode();
            setSize(dm.getWidth(), dm.getHeight());
            isFullScreen = true;
        }

//        canvas.setSize(dm.getWidth(), dm.getHeight());
    }

    private void messageBox(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void run() {
        emulatorScreen.repaint();
        if (debugger != null) {
            debugger.repaint();
        }
        if (spriteMemoryViewer != null) {
            spriteMemoryViewer.repaint();
        }
        if (patternTableViewer != null) {
            patternTableViewer.repaint();
        }
        if (nameTableViewer != null) {
            nameTableViewer.repaint();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel (new MaterialLookAndFeel(new MaterialOrientalFontsTheme()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Emulator frame = new Emulator();
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("NES file", "nes"));
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(new File("./src/main/resources"));
        fc.showOpenDialog(frame);
        if (fc.getSelectedFile() != null) {
            frame.startGame(fc.getSelectedFile().getAbsolutePath());
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
    }
}
