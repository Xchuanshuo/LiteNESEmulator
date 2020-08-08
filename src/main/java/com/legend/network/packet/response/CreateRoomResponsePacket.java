package com.legend.network.packet.response;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 创建房间响应数据包
 */
public class CreateRoomResponsePacket implements Packet {

    private int roomId;
    private String msg;
    private String gameMd5;

    public CreateRoomResponsePacket(int roomId, String msg) {
        this.roomId = roomId;
        this.msg = msg;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getGameMd5() {
        return gameMd5;
    }

    public void setGameMd5(String gameMd5) {
        this.gameMd5 = gameMd5;
    }

    @Override
    public int getType() {
        return RESPONSE_CREATE_ROOM;
    }
}
