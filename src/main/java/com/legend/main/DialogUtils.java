package com.legend.main;

import com.legend.network.Configuration;
import com.legend.utils.Constants;
import com.legend.utils.PropertiesUtils;
import com.legend.utils.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Legend
 * @data by on 20-8-8.
 * @description 弹窗工具类
 */
public class DialogUtils {

    public static void showCreateRoomDialog(Frame owner, CreateRoomCallback callback) {
        final JDialog dialog = new JDialog(owner, "创建房间", true);
        dialog.setSize(250, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        JLabel gamePathLabel = new JLabel("游戏: ");
        gamePathLabel.setBounds(20, 20, 66, 30);
        JTextField filePathTextField = new JTextField("");
        filePathTextField.setEditable(false);
        filePathTextField.setBounds(60, 20, 110, 30);

        // 创建一个按钮用于关闭对话框
        JButton fileSelectBtn = new JButton("选择");
        fileSelectBtn.setBounds(175, 20, 50, 30);
        fileSelectBtn.addActionListener(e -> {
            fileSelectAndSet(owner, filePathTextField);
        });

        JButton confirmBtn = new JButton("确认");
        confirmBtn.setBounds(80, 60, 70, 30);
        confirmBtn.addActionListener(e -> {
            if (callback != null) {
                try {
                    callback.onCallback(filePathTextField.getText().trim());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            dialog.dispose();
        });

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // 添加组件到面板
        panel.add(gamePathLabel);
        panel.add(filePathTextField);
        panel.add(fileSelectBtn);
        panel.add(confirmBtn);

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);
    }

    public static void showJoinRoomDialog(Frame owner, JoinRoomCallback callback) {
        final JDialog dialog = new JDialog(owner, "加入房间", true);
        dialog.setSize(250, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        // 创建一个标签显示消息内容
        JLabel roomIdLabel = new JLabel("房间号: ");
        roomIdLabel.setBounds(20, 10, 66, 30);

        JFormattedTextField roomIdTextField = new JFormattedTextField();
        roomIdTextField.setBounds(75, 10, 110, 30);
        roomIdTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if (!(keyChar >= KeyEvent.VK_0 &&
                        keyChar<=KeyEvent.VK_9)) {
                    e.consume();
                }
            }
        });

        JLabel gamePathLabel = new JLabel("游戏: ");
        gamePathLabel.setBounds(20, 50, 66, 30);
        JTextField filePathTextField = new JTextField("");
        filePathTextField.setEditable(false);
        filePathTextField.setBounds(75, 50, 110, 30);

        // 创建一个按钮用于关闭对话框
        JButton fileSelectBtn = new JButton("选择");
        fileSelectBtn.setBounds(190, 50, 45, 30);
        fileSelectBtn.addActionListener(e -> {
            fileSelectAndSet(owner, filePathTextField);
        });

        JButton confirmBtn = new JButton("确认");
        confirmBtn.setBounds(100, 85, 45, 30);
        confirmBtn.addActionListener(e -> {
            System.out.println("房间号: " + roomIdTextField.getText()
                    + "\n游戏本地路径: " + filePathTextField.getText());
            if (callback != null) {
                try {
                    callback.onCallback(roomIdTextField.getText().trim(),
                            filePathTextField.getText().trim());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            dialog.dispose();
        });

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // 添加组件到面板
        panel.add(roomIdLabel);
        panel.add(roomIdTextField);
        panel.add(gamePathLabel);
        panel.add(filePathTextField);
        panel.add(fileSelectBtn);
        panel.add(confirmBtn);

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);
    }

    public static void showServerConfigurationDialog(Frame owner) {
        final JDialog dialog = new JDialog(owner, "配置服务器", true);
        dialog.setSize(250, 150);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);

        // 创建一个标签显示消息内容
        JLabel ipLabel = new JLabel("IP地址: ");
        ipLabel.setBounds(20, 10, 66, 30);
        JFormattedTextField ipTextField = new JFormattedTextField();
        ipTextField.setText(Configuration.SERVER_ADDRESS);
        ipTextField.setBounds(75, 10, 110, 30);

        JLabel portLabel = new JLabel("端口号: ");
        portLabel.setBounds(20, 50, 66, 30);
        JTextField portTextField = new JTextField("");
        portTextField.setText(String.valueOf(Configuration.SERVER_PORT));
        portTextField.setBounds(75, 50, 110, 30);
        portTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int keyChar = e.getKeyChar();
                if (!(keyChar >= KeyEvent.VK_0 &&
                        keyChar<=KeyEvent.VK_9)) {
                    e.consume();
                }
            }
        });

        JButton confirmBtn = new JButton("确认");
        confirmBtn.setBounds(100, 85, 45, 30);
        confirmBtn.addActionListener(e -> {
            System.out.println("IP地址: " + ipTextField.getText()
                    + "\n端口号: " + portTextField.getText());
            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());
            Configuration.SERVER_ADDRESS = ip;
            Configuration.SERVER_PORT = port;
            PropertiesUtils.put(Configuration.ADDRESS, ip);
            PropertiesUtils.put(Configuration.PORT, String.valueOf(port));
            dialog.dispose();
        });

        JPanel panel = new JPanel();
        panel.setLayout(null);

        // 添加组件到面板
        panel.add(ipLabel);
        panel.add(ipTextField);
        panel.add(portLabel);
        panel.add(portTextField);
        panel.add(confirmBtn);

        // 设置对话框的内容面板
        dialog.setContentPane(panel);
        // 显示对话框
        dialog.setVisible(true);
    }

    private static void fileSelectAndSet(Frame frame, JTextField textField) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("NES file", "nes"));
        fc.setMultiSelectionEnabled(false);
        String path = PropertiesUtils.get(Constants.LAST_OPEN_PATH);
        if (StringUtils.isEmpty(path)) {
            path = "./src/main/resources";
        }
        fc.setCurrentDirectory(new File(path));
        fc.showOpenDialog(frame);
        if (fc.getSelectedFile() != null) {
            textField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    public interface CreateRoomCallback {
        void onCallback(String filePath) throws IOException;
    }

    public interface JoinRoomCallback {
        void onCallback(String roomIdStr, String filePath) throws IOException;
    }
}
