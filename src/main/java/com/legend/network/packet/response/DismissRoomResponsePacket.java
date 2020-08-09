package com.legend.network.packet.response;

import com.legend.network.Configuration;
import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-8.
 * @description 解散房间响应包
 */
public class DismissRoomResponsePacket implements Packet {

    private int roomId;
    private int code;
    private String msg;

    public DismissRoomResponsePacket(int roomId, int code, String msg) {
        this.roomId = roomId;
        this.code = code;
        this.msg = msg;
    }

    public DismissRoomResponsePacket(int roomId) {
        this(roomId, Configuration.FAILURE, "");
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public int getType() {
        return RESPONSE_DISMISS_ROOM;
    }
}
