package com.legend.network;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Legend
 * @data by on 20-8-6.
 * @description 游戏房间
 */
public class Room {

    private int id;
    private String gameName;
    private String gameMd5;
    private int masterId;
    private String password;
    private List<Client> clients = new ArrayList<>();

    public Room(int id, int masterId, String password) {
        this.id = id;
        this.masterId = masterId;
        this.password = password;
    }

    public Room(int id, int masterId) {
        this(id, masterId, "");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getMasterId() {
        return masterId;
    }

    public void setMasterId(int masterId) {
        this.masterId = masterId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public static class Client {
        private String ip;
        private int port;

        public Client(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        @Override
        public String toString() {
            return "NetClient{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }
    }
}
