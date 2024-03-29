package com.legend.main;

import com.legend.cartridges.FileNesLoader;
import com.legend.common.EmulatorShutdownHook;
import com.legend.input.StandardControllers;
import com.legend.main.operation.SpeedFrame;
import com.legend.main.tools.Debugger;
import com.legend.main.tools.NameTableViewer;
import com.legend.main.tools.PatternTableViewer;
import com.legend.main.tools.SpriteMemoryViewer;
import com.legend.memory.IMemory;
import com.legend.memory.MemoryLock;
import com.legend.memory.StandardMemory;
import com.legend.network.NetClient;
import com.legend.storage.LocalStorage;
import com.legend.utils.Constants;
import com.legend.utils.PropertiesUtils;
import com.legend.utils.StringUtils;
import com.legend.utils.XBRZ;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.legend.network.Configuration.SUCCESS;
import static com.legend.ppu.IPPU.SCREEN_HEIGHT;
import static com.legend.ppu.IPPU.SCREEN_WIDTH;
import static com.legend.utils.Constants.*;
import static javax.swing.JOptionPane.YES_NO_OPTION;

/**
 * @author Legend
 * @data by on 20-4-18.
 * @description
 */
public class Emulator extends JFrame implements Runnable, KeyListener, NetClient.Callback {

    public static volatile double CPU_CYCLE_PER_SECOND = 1789772.5;
    public static int SPEAKER_SAMPLE_RATE = 44100;

    private GameRunner gameRunner;
    private int curRoomId = -1;
    private NetClient netClient = new NetClient();
    public static StandardControllers controllers = new StandardControllers();
    private EmulatorScreen emulatorScreen = new EmulatorScreen();
    private EmulatorSpeaker emulatorSpeaker = new EmulatorSpeaker();

    private Map<Integer, Integer> keyBindings = new HashMap<>();

    private JMenuBar jMenuBar;
    private boolean isFullScreen = false;

    private LocalStorage storage = new LocalStorage();

    private SpeedFrame speedFrame;

    private Debugger debugger;
    private SpriteMemoryViewer spriteMemoryViewer;
    private PatternTableViewer patternTableViewer;
    private NameTableViewer nameTableViewer;
    private int pending = 0;

    public Emulator() {
        SwingUtilities.invokeLater(this::initFrame);
        initKeyboardBinds();
        Runtime.getRuntime().addShutdownHook(new EmulatorShutdownHook());
        // 在本地搜索该文件并加载游戏
        netClient.setStatusCallback(this);
    }

    private void initKeyboardBinds() {
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
        // debug方式启动
//        gameRunner.pause();
    }

    private void searchAndLoadRom(String gameMd5) {
        try {
            String path = searchPathByMd5(PropertiesUtils.get(LAST_OPEN_PATH), gameMd5);
            System.out.println("搜索到文件路径: " + path);
            if (!StringUtils.isEmpty(path)) {
                startGame(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFrame() {
        emulatorScreen.setPreferredSize(new Dimension(SCREEN_WIDTH * 3, SCREEN_HEIGHT * 3));
        emulatorScreen.setFocusable(false);

        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getButton()) {
                    case MouseEvent.BUTTON1:
                        System.out.println("鼠标左键！");
                        break;
                    case MouseEvent.BUTTON2:
                        System.out.println("鼠标中键!");
                        break;
                    case MouseEvent.BUTTON3:
                        System.out.println("鼠标右键！");
                        break;
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                System.out.println("滚动！");
            }
        });
        initMenuBar();
        setLayout(new BorderLayout());
        add(emulatorScreen);
        pack();
        setTitle(MAIN_TITLE);
        setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        setLocationRelativeTo(null);
    }

    private void initMenuBar() {
        jMenuBar = new JMenuBar();

        JMenu fileMenu = getFileMenu();
        JMenu operationMenu = getOperationMenu();
        JMenu networkMenu = getNetWorkMenu();
        JMenu toolsMenu = getToolsMenu();

        jMenuBar.add(fileMenu);
        jMenuBar.add(operationMenu);
        jMenuBar.add(networkMenu);
        jMenuBar.add(toolsMenu);
        setJMenuBar(jMenuBar);
    }

    private JMenu getNetWorkMenu() {
        JMenu fileMenu = new JMenu("Network");
        JMenuItem createRoomItem = new JMenuItem(CREATE_ROOM);
        JMenuItem joinRoomItem = new JMenuItem(JOIN_ROOM);
        JMenuItem curRoomItem = new JMenuItem(CURRENT_ROOM);
        JMenuItem exitItem = new JMenuItem(EXIT_ROOM);
        JMenuItem dismissItem = new JMenuItem(DISMISS_ROOM);
        JMenuItem configureServerItem = new JMenuItem(CONFIGURE_SERVER);
        fileMenu.add(createRoomItem);
        fileMenu.add(joinRoomItem);
        fileMenu.add(curRoomItem);
        fileMenu.add(exitItem);
        fileMenu.add(dismissItem);
        fileMenu.add(configureServerItem);
        createRoomItem.addActionListener(networkListener);
        joinRoomItem.addActionListener(networkListener);
        curRoomItem.addActionListener(networkListener);
        exitItem.addActionListener(networkListener);
        dismissItem.addActionListener(networkListener);
        configureServerItem.addActionListener(networkListener);
        return fileMenu;
    }

    private JMenu getFileMenu() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadRomItem = new JMenuItem(LOAD_ROM);
        JMenuItem closeRomItem = new JMenuItem(CLOSE_ROM);
        JMenuItem saveStatusItem = new JMenuItem(SAVE_STATUS);
        JMenuItem loadStatusItem = new JMenuItem(LOAD_STATUS);
        JMenuItem exitItem = new JMenuItem(EXIT);
        fileMenu.add(loadRomItem);
        fileMenu.add(closeRomItem);
        fileMenu.add(saveStatusItem);
        fileMenu.add(loadStatusItem);
        fileMenu.add(exitItem);
        loadRomItem.addActionListener(fileListener);
        closeRomItem.addActionListener(fileListener);
        saveStatusItem.addActionListener(fileListener);
        loadStatusItem.addActionListener(fileListener);
        exitItem.addActionListener(fileListener);
        return fileMenu;
    }

    private JMenu getOperationMenu() {
        JMenu operationMenu = new JMenu("Operation");
        JMenuItem resetItem = new JMenuItem(RESET);
        JMenuItem cheatItem = new JMenuItem(CHEAT);
        JMenuItem speedItem = new JMenuItem(SPEED);
        JMenu imageQualityItem = new JMenu(IMAGE_QUALITY);
        ButtonGroup imageQualityGroup = new ButtonGroup();

        JRadioButtonMenuItem lowQuality = new JRadioButtonMenuItem(QUALITY_LOW);
        JRadioButtonMenuItem midQuality = new JRadioButtonMenuItem(QUALITY_MID, true);
        JRadioButtonMenuItem highQuality = new JRadioButtonMenuItem(QUALITY_HIGH);
        lowQuality.addActionListener(qualitySelectListener);
        midQuality.addActionListener(qualitySelectListener);
        highQuality.addActionListener(qualitySelectListener);
        imageQualityGroup.add(lowQuality);
        imageQualityGroup.add(midQuality);
        imageQualityGroup.add(highQuality);
        imageQualityItem.add(lowQuality);
        imageQualityItem.add(midQuality);
        imageQualityItem.add(highQuality);

        JMenuItem fullScreenItem = new JMenuItem(FULL_SCREEN);
        operationMenu.add(resetItem);
        operationMenu.add(cheatItem);
        operationMenu.add(speedItem);
        operationMenu.add(imageQualityItem);
        operationMenu.add(fullScreenItem);
        resetItem.addActionListener(operationListener);
        cheatItem.addActionListener(operationListener);
        speedItem.addActionListener(operationListener);
        fullScreenItem.addActionListener(operationListener);
        return operationMenu;
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
            case LOAD_ROM:
                System.out.println("load rom");
                selectAndLoadRom();
                break;
            case CLOSE_ROM:
                stop();
                break;
            case SAVE_STATUS:
            case LOAD_STATUS:
                saveOrLoad();
                break;
            case EXIT:
                System.exit(1);
                break;
            default: break;
        }
    };

    private ActionListener operationListener = e -> {
        switch (e.getActionCommand()) {
            case RESET:
                if (gameRunner != null) {
                    MemoryLock.clearAll();
                    gameRunner.onReset();
                }
                break;
            case CHEAT:
                cheat();
                break;
            case SPEED:
                speed();
                break;
            case FULL_SCREEN:
                fullScreen();
                break;
            default: break;
        }
    };

    private ActionListener networkListener = e -> {
        switch (e.getActionCommand()) {
            case CREATE_ROOM:
                DialogUtils.showCreateRoomDialog(this, filePath -> {
                    if (!StringUtils.isEmpty(filePath)) {
                        netClient.sendCreateRoomMsg(new FileNesLoader(filePath).getFileMD5());
                    }
                });
                break;
            case JOIN_ROOM:
                DialogUtils.showJoinRoomDialog(this, (roomIdStr, filePath) -> {
                    if (StringUtils.isHexNumeric(roomIdStr)
                            && !StringUtils.isEmpty(filePath)) {
                        netClient.sendJoinRoomMsg(Integer.parseInt(roomIdStr),
                                    new FileNesLoader(filePath).getFileMD5());
                    } else {
                        JOptionPane.showMessageDialog(null, "非法输入, 房间号" +
                                "只能为数字!且本地文件不能为空！");
                    }
                });
                break;
            case CURRENT_ROOM:
                JOptionPane.showMessageDialog(null,
                        "当前已经加入入的房间号---【" + curRoomId + "】");
                break;
            case EXIT_ROOM:
                int code = JOptionPane.showConfirmDialog(null,
                        "您确认要退出房间【" + curRoomId + "】吗？", "", YES_NO_OPTION);
                if (code == 0) netClient.sendExitRoomMsg();
                break;
            case DISMISS_ROOM:
                code = JOptionPane.showConfirmDialog(null,
                        "您确认要解散房间【" + curRoomId + "】吗？", "", YES_NO_OPTION);
                if (code == 0) netClient.sendDismissRoomMsg();
                break;
            case CONFIGURE_SERVER:
                DialogUtils.showServerConfigurationDialog(this);
                break;
            default: break;
        }
    };

    private void speed() {
        if (speedFrame == null) {
            speedFrame = new SpeedFrame();
            speedFrame.setSpeedListener(speed -> {
                if (gameRunner != null) {
                    CPU_CYCLE_PER_SECOND = speed;
                    gameRunner.initCycle();
                }
            });
        } else {
            speedFrame.setVisible(true);
        }
    }

    private ActionListener qualitySelectListener = e -> {
        switch (e.getActionCommand()) {
            case QUALITY_LOW:
                emulatorScreen.setScaleSize(null);
                break;
            case QUALITY_MID:
                emulatorScreen.setScaleSize(XBRZ.ScaleSize.Times2);
                break;
            case QUALITY_HIGH:
                emulatorScreen.setScaleSize(XBRZ.ScaleSize.Times4);
                break;
            default: break;
        }
    };

    private ActionListener toolsListener = e -> {
        switch (e.getActionCommand()) {
            case DEBUG:
                if (debugger == null) {
                    debugger = new Debugger(gameRunner);
                } else {
                    debugger.setVisible(true);
                }
                gameRunner.setStepInto(true);
                System.out.println("debug");
                break;
            case PATTERN_TABLES_VIEWER:
                if (patternTableViewer == null) {
                    patternTableViewer = new PatternTableViewer(gameRunner);
                } else {
                    patternTableViewer.setVisible(true);
                }
                break;
            case SPRM:
                if (spriteMemoryViewer == null) {
                    spriteMemoryViewer = new SpriteMemoryViewer(gameRunner);
                } else {
                    spriteMemoryViewer.setVisible(true);
                }
                break;
            case NAME_TABLES_VIEWER:
                gameRunner.pause();
                if (nameTableViewer == null) {
                    nameTableViewer = new NameTableViewer(gameRunner);
                } else {
                    nameTableViewer.setVisible(true);
                }
                gameRunner.resume();
                break;
            default: break;
        }
    };

    private void cheat() {
        String addressStr = JOptionPane.showInputDialog("请输入作弊码:", "");
        if (StringUtils.isEmpty(addressStr)) return;
        String[] str = addressStr.split("-");
        if (str.length == 3) {
            int address = Integer.parseInt(str[0], 16);
            int count = Integer.parseInt(str[1], 16);
            if (str[2].length() % 2 != 0) {
                str[2] = "0" + str[2];
            }
            for (int i = 0;i < count;i++) {
                if (gameRunner != null) {
                    IMemory memory = gameRunner.getCPU().getMemory();
                    int val = Integer.parseInt(str[2].substring(i * 2, i * 2 + 2), 16);
                    // 1.内存已经锁定则先解锁 2.设置内存值 3.重新上锁
                    MemoryLock.unLock(address + i);
                    memory.writeByte(address + i, val);
                    MemoryLock.lock(address + i);
                }
            }
        }
    }

    private void selectAndLoadRom() {
        if (gameRunner != null) {
            gameRunner.pause();
        }
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("NES file", "nes"));
        fc.setMultiSelectionEnabled(false);
        String path = PropertiesUtils.get(Constants.LAST_OPEN_PATH);
        if (StringUtils.isEmpty(path)) {
            path = "./src/main/resources";
        }
        fc.setCurrentDirectory(new File(path));
        fc.showOpenDialog(this);
        if (gameRunner != null) {
            gameRunner.resume();
        }
        if (fc.getSelectedFile() != null) {
            stop();
            MemoryLock.clearAll();
            PropertiesUtils.put(LAST_OPEN_PATH, fc.getSelectedFile().getParent());
            startGame(fc.getSelectedFile().getAbsolutePath());
        } else {
            System.out.println("文件为空！");
        }
    }

    private static Map<String, String> gameFileMap = new HashMap<>();

    private String searchPathByMd5(String path, String md5) throws IOException {
        if (gameFileMap.containsKey(md5)) {
            String gameFilePath = gameFileMap.get(md5);
            File file = new File(gameFilePath);
            if (file.exists() && file.isFile() &&
                file.getName().toLowerCase().equals(".nes")) {
                return gameFilePath;
            }
        }
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                String fileName = f.getName().toLowerCase();
                if (f.isFile() && fileName.endsWith(".nes")) {
                    System.out.println("file Path:" + f.getPath());
                    FileNesLoader loader = new FileNesLoader(f);
                    gameFileMap.put(loader.getFileMD5(), f.getAbsolutePath());
                    if (loader.getFileMD5().equals(md5)) {
                        return f.getAbsolutePath();
                    }
                } else {
                    searchPathByMd5(f.getAbsolutePath(), md5);
                }
            }
        }
        return null;
    }

    private void stop() {
        if (gameRunner != null) {
            gameRunner.stop();
            if (emulatorSpeaker != null) {
                emulatorSpeaker.stop();
            }
            if (emulatorScreen != null) {
                emulatorScreen.reset();
            }
        }
    }

    private void reloadGame(String path) {
        System.out.println("加载游戏存档: " + path);
        if (gameRunner != null) {
            System.out.println("开始加载");
            storage.load(gameRunner);
            // 注意手柄也需要更新为新加载的
            controllers = (StandardControllers) ((StandardMemory) gameRunner.getCPU()
                    .getMemory()).getMemory(0x4016);
            System.out.println("加载完成");
        }
    }

    private void saveOrLoad() {
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
        if (gameRunner != null) {
            fileNameFilter.setName(gameRunner.getLoader().getFileMD5());
        }
        fc.setFileFilter(fileNameFilter);
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(new File(Constants.GLOBAL_SL_DIR));
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
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        Integer r = keyBindings.get(e.getKeyCode());
        if (r != null) {
            r = processNetWork(r, true);
            controllers.press((r / 8) & 1, (r % 8) & 0xFF);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Integer r = keyBindings.get(e.getKeyCode());
        if (r != null) {
            r = processNetWork(r, false);
            controllers.release((r / 8) & 1, (r % 8) & 0xFF);
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_M:
                saveOrLoad();
                break;
            case KeyEvent.VK_P:
                fullScreen();
                break;
            case KeyEvent.VK_C:
                cheat();
            default: break;
        }
    }

    private int processNetWork(int r, boolean isPressed) {
        if (netClient.isOnline()) {
            if (!netClient.isMaster()) {
                r = r + 8;
            }
            netClient.sendInputMsg(r, isPressed);
        }
        return r;
    }

    private void fullScreen() {
        if (isFullScreen) {
            this.dispose();
            setSize(SCREEN_WIDTH * 3, SCREEN_HEIGHT * 3);
            setUndecorated(false);
            jMenuBar.setVisible(true);
            setVisible(true);
            isFullScreen = false;
        } else {
            GraphicsDevice gd = getGraphicsConfiguration().getDevice();
            if (!gd.isFullScreenSupported()) {
                return;
            }
            this.dispose();
            this.setUndecorated(true);
            jMenuBar.setVisible(false);
            gd.setFullScreenWindow(this);
            setVisible(true);
            DisplayMode dm = gd.getDisplayMode();
            setSize(dm.getWidth(), dm.getHeight());
            isFullScreen = true;
        }
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
        if (pending % 20 == 0) {
            if (patternTableViewer != null) {
                patternTableViewer.repaint();
            }
            if (nameTableViewer != null) {
                nameTableViewer.repaint();
            }
        }
        if (pending % 60 == 0) {
            setTitle(MAIN_TITLE + "-" + gameRunner.getFps() + "fps");
        }
        pending++;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel (new MaterialLookAndFeel(new MaterialOrientalFontsTheme()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Emulator frame = new Emulator();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        frame.selectAndLoadRom();
        frame.setVisible(true);
    }

    @Override
    public void onJoinRoomCallback(NetClient.Response response) {
        if (response.getCode() == SUCCESS) {
            searchAndLoadRom(response.getGameMd5());
            curRoomId = response.getRoomId();
        }
        JOptionPane.showMessageDialog(null, response.getMsg()
                + "---【房间号】" + response.getRoomId());
    }

    @Override
    public void onExitRoomCallback(int code, String msg) {
        if (code == SUCCESS) {
            curRoomId = -1;
            stop();
        }
        JOptionPane.showMessageDialog(null, msg);
    }
}
