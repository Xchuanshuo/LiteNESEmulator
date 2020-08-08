package com.legend.network;

import com.legend.main.Emulator;
import com.legend.network.packet.Packet;
import com.legend.network.packet.request.CreateRoomRequestPacket;
import com.legend.network.packet.request.InputOperationRequestPacket;
import com.legend.network.packet.request.JoinRoomRequestPacket;
import com.legend.network.packet.response.CreateRoomResponsePacket;
import com.legend.network.packet.response.JoinRoomResponsePacket;
import com.legend.utils.IdUtils;

import static com.legend.network.packet.Packet.*;

/**
 * @author Legend
 * @data by on 20-8-6.
 * @description 网络客户端
 */
public class NetClient implements ReceiveCallback {

    private int id = -1;
    private boolean isOnline = false;
    // 是否是房主
    private boolean isMaster = false;
    // 已经加入房间id, 仅在isOnline为true的情况下有用
    private int joinedRoomId = -1;
    private JoinRoomCallback joinRoomCallback;
    private ClientTaskHandler handler = ClientTaskHandler.getInstance();

    public NetClient() {
        this.id = IdUtils.getRandomId();
        handler.setCallback(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public ClientTaskHandler getHandler() {
        return handler;
    }

    public void setJoinRoomCallback(JoinRoomCallback joinRoomCallback) {
        this.joinRoomCallback = joinRoomCallback;
    }

    public interface JoinRoomCallback {
        void onCallback(Response response);
    }

    @Override
    public void onReceive(Packet packet) {
        switch (packet.getType()) {
            case RESPONSE_CREATE_ROOM:
                CreateRoomResponsePacket crrp = (CreateRoomResponsePacket) packet;
                System.out.println(crrp.getMsg());
                sendJoinRoomMsg(crrp.getRoomId(), crrp.getGameMd5());
                break;
            case RESPONSE_JOIN_ROOM:
                JoinRoomResponsePacket jrrp = (JoinRoomResponsePacket) packet;
                isOnline = true;
                joinedRoomId = jrrp.getRoomId();
                isMaster = jrrp.isMaster();
                System.out.println("收到加入房间响应包: " + jrrp.getCode() + "---" + jrrp.getMsg()
                        + ", md5=" + jrrp.getGameMd5());
                if (joinRoomCallback != null) {
                    Response response = new Response(jrrp.getCode(), jrrp.getRoomId(),
                            jrrp.getMsg(), jrrp.getGameMd5());
                    joinRoomCallback.onCallback(response);
                }
                break;
            case REQUEST_INPUT_OPERATION:
                InputOperationRequestPacket iprp = (InputOperationRequestPacket) packet;
                if (iprp.getPlayId() != id) {
                    // 过滤掉自己发送的数据包
                    if (isOnline && joinedRoomId == iprp.getRoomId()) {
                        int keyVal = iprp.getKey();
                        boolean isPressed = iprp.isPressed();
                        if (isPressed) {
                            Emulator.controllers.press((keyVal/8) & 1, (keyVal % 8) & 0xFF);
                        } else {
                            Emulator.controllers.release((keyVal/8) & 1, (keyVal % 8) & 0xFF);
                        }
                    }
                }
                break;
            default: break;
        }
    }

    public void sendInputMsg(int key, boolean isPressed) {
        if (!isOnline) {
            System.out.println("当前设备不在线------ID----" + id);
            return;
        }
        InputOperationRequestPacket requestPacket = new InputOperationRequestPacket();
        requestPacket.setKey(key);
        requestPacket.setPressed(isPressed);
        requestPacket.setPlayId(id);
        requestPacket.setRoomId(joinedRoomId);
        handler.send(requestPacket);
    }

    public void sendCreateRoomMsg(String gameMd5) {
        CreateRoomRequestPacket requestPacket = new CreateRoomRequestPacket();
        requestPacket.setPassword("");
        requestPacket.setUserId(id);
        requestPacket.setGameMd5(gameMd5);
        handler.send(requestPacket);
    }

    public void sendJoinRoomMsg(int roomId, String gameMd5) {
        if (joinedRoomId == roomId) {
            System.out.println("不允许重复加入!");
            return;
        }
        JoinRoomRequestPacket requestPacket = new JoinRoomRequestPacket(roomId, id, gameMd5);
        System.out.println(requestPacket);
        handler.send(requestPacket);
    }

    public class Response {
        private int code;
        private int roomId;
        private String msg;
        private String gameMd5;

        public Response(int code, int roomId, String msg, String gameMd5) {
            this.code = code;
            this.roomId = roomId;
            this.msg = msg;
            this.gameMd5 = gameMd5;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
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
    }

}
