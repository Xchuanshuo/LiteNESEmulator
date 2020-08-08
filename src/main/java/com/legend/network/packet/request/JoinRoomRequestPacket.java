package com.legend.network.packet.request;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 加入房间请求包
 */
public class JoinRoomRequestPacket implements Packet {

    private int roomId;
    private int userId;
    private String gameMd5;

    public JoinRoomRequestPacket(int roomId, int userId, String gameMd5) {
        this.roomId = roomId;
        this.userId = userId;
        this.gameMd5 = gameMd5;
        System.out.println(gameMd5);
    }

    public String getGameMd5() {
        return gameMd5;
    }

    public void setGameMd5(String gameMd5) {
        this.gameMd5 = gameMd5;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int getType() {
        return REQUEST_JOIN_ROOM;
    }

    @Override
    public String toString() {
        return "JoinRoomRequestPacket{" +
                "roomId=" + roomId +
                ", userId=" + userId +
                ", gameMd5='" + gameMd5 + '\'' +
                '}';
    }
}
