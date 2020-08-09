package com.legend.network.packet.request;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-8.
 * @description 解散房间请求包
 */
public class DismissRoomRequestPacket implements Packet {

    private int userId;
    private int roomId;

    public DismissRoomRequestPacket(int userId, int roomId) {
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
    public int getType() {
        return REQUEST_DISMISS_ROOM;
    }
}
