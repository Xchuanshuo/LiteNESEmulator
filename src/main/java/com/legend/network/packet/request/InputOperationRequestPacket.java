package com.legend.network.packet.request;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 输入(手柄)操作数据包(此数据包无需响应)
 */
public class InputOperationRequestPacket implements Packet {

    // 游戏房间id
    private int roomId;
    // 玩家id
    private int playId;
    // 按键
    private int key;
    // 是否是按下
    private boolean isPressed;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getPlayId() {
        return playId;
    }

    public void setPlayId(int playId) {
        this.playId = playId;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean pressed) {
        isPressed = pressed;
    }

    @Override
    public int getType() {
        return REQUEST_INPUT_OPERATION;
    }
}
