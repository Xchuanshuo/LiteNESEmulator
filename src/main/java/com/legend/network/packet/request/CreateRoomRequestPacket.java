package com.legend.network.packet.request;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 创建房间请求数据包
 */
public class CreateRoomRequestPacket implements Packet {

    // 请求用户id
    private int userId;
    // 密码
    private String password;
    // 游戏名称
    private String gameName;
    // 游戏md5
    private String gameMd5;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameMd5() {
        return gameMd5;
    }

    public void setGameMd5(String gameMd5) {
        this.gameMd5 = gameMd5;
    }

    @Override
    public int getType() {
        return REQUEST_CREATE_ROOM;
    }
}
