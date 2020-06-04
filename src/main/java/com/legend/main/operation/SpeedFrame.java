package com.legend.main.operation;

import com.legend.main.Emulator;
import com.legend.main.GameRunner;
import com.legend.utils.Constants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Hashtable;

/**
 * @author Legend
 * @data by on 20-6-4.
 * @description
 */
public class SpeedFrame extends JFrame {

    private static final double SPEED = 1789772.5;

    public SpeedFrame() {
        initView();
    }

    private void initView() {
        setTitle(Constants.SPEED);
        setResizable(false);
        setSize(500, 260);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        // 创建一个滑块，最小值、最大值、初始值 分别为 0、20、10
        final JSlider slider = new JSlider(2, 8, 2);
        // 设置主刻度间隔
        slider.setMajorTickSpacing(1);
        // 绘制 刻度 和 标签
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // 给指定的刻度值显示自定义标签
        Hashtable<Integer, JComponent> hashtable = new Hashtable<>();
        hashtable.put(1, new JLabel("0.5"));
        hashtable.put(2, new JLabel("1.0"));
        hashtable.put(3, new JLabel("1.5"));
        hashtable.put(4, new JLabel("2.0"));
        hashtable.put(5, new JLabel("2.5"));
        hashtable.put(6, new JLabel("3.0"));
        hashtable.put(7, new JLabel("3.5"));
        hashtable.put(8, new JLabel("4.0"));

        slider.setLabelTable(hashtable);

        // 添加刻度改变监听器
        slider.addChangeListener(e -> Emulator.CPU_CYCLE_PER_SECOND = SPEED *  slider.getValue() * 0.5);
        // 添加滑块到内容面板
        panel.add(slider);
        setContentPane(panel);

        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

}
