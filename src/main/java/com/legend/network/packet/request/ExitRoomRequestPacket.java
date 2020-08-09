package com.legend.network.packet.request;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-8.
 * @description 退出房间请求包
 */
public class ExitRoomRequestPacket implements Packet {

    private int userId;
    private int roomId;

    public ExitRoomRequestPacket(int userId, int roomId) {
        this.userId = userId;
        this.roomId = roomId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public String toString() {
        return "ExitRoomRequestPacket{" +
                "userId=" + userId +
                ", roomId=" + roomId +
                '}';
    }

    @Override
    public int getType() {
        return REQUEST_EXIT_ROOM;
    }
}
