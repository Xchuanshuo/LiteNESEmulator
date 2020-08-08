package com.legend.network.packet.response;

import com.legend.network.Configuration;
import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 加入房间响应包
 */
public class JoinRoomResponsePacket implements Packet {

    private int roomId;
    // 是否是主机, 客户端响应后保存该状态标志
    private boolean isMaster;
    private String gameMd5;
    private int code;
    private String msg;

    public JoinRoomResponsePacket(int roomId, boolean isMaster, int code, String msg) {
        this.roomId = roomId;
        this.isMaster = isMaster;
        this.code = code;
        this.msg = msg;
    }

    public JoinRoomResponsePacket(int roomId, boolean isMaster, int code) {
        this(roomId, isMaster, code, "");
    }

    public JoinRoomResponsePacket(int roomId, boolean isMaster) {
        this(roomId, isMaster, Configuration.FAILURE);
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
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

    public String getGameMd5() {
        return gameMd5;
    }

    public void setGameMd5(String gameMd5) {
        this.gameMd5 = gameMd5;
    }

    @Override
    public int getType() {
        return RESPONSE_JOIN_ROOM;
    }

    @Override
    public String toString() {
        return "JoinRoomResponsePacket{" +
                "roomId=" + roomId +
                ", isMaster=" + isMaster +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
