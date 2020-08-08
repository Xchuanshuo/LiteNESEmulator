package com.legend.network;

import com.legend.network.packet.Packet;
import com.legend.network.packet.request.CreateRoomRequestPacket;
import com.legend.network.packet.request.InputOperationRequestPacket;
import com.legend.network.packet.request.JoinRoomRequestPacket;
import com.legend.network.packet.response.CreateRoomResponsePacket;
import com.legend.network.packet.response.JoinRoomResponsePacket;
import com.legend.utils.IdUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static com.legend.network.packet.Packet.*;


/**
 * @author Legend
 * @data by on 20-8-6.
 * @description 服务器 TCP建立连接 UDP数据转发
 */
public class Server {

    private static final Map<Integer, Room> roomMap = new HashMap<>();
    private static int UDP_PORT = Configuration.SERVER_PORT;

    public static void main(String[] args) {
        new Thread(new UDPServer()).start();
    }

    private static class UDPServer implements Runnable {

        byte[] buf = new byte[1024];
        private DatagramSocket ds = null;

        @Override
        public void run() {
            try {
                ds = new DatagramSocket(UDP_PORT);
                System.out.println("The Game Server is started...");
                while (!ds.isClosed()) {
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    ds.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                    ObjectInputStream o = new ObjectInputStream(bais);
                    Packet packet = (Packet) o.readObject();
                    System.out.println("Message From: " +
                            dp.getAddress().getHostAddress() + "---" + dp.getPort());
                    System.out.println(packet);
                    processPacket(packet, dp.getAddress().getHostName(), dp.getPort());
                    bais.close();
                    o.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void processPacket(Packet packet, String fromIP, int fromPort) throws IOException {
            switch (packet.getType()) {
                case REQUEST_CREATE_ROOM:
                    System.out.println("收到创建房间请求, " + fromIP + ":" + fromPort);
                    processCreateRoom((CreateRoomRequestPacket) packet, fromIP, fromPort);
                    break;
                case REQUEST_JOIN_ROOM:
                    System.out.println("收到加入房间请求, " + fromIP + ":" + fromPort);
                    processJoinRoom((JoinRoomRequestPacket)packet, fromIP, fromPort);
                    break;
                case REQUEST_EXIT_ROOM:
                    break;
                case REQUEST_DISMISS_ROOM:
                    break;
                case REQUEST_INPUT_OPERATION:
                    InputOperationRequestPacket iorp = (InputOperationRequestPacket) packet;
                    Room room = roomMap.get(iorp.getRoomId());
                    if (room == null) {
                        System.err.println("房间【" + iorp.getRoomId() + "为空!");
                    } else {
                        DatagramPacket sentPacket = new DatagramPacket(buf, buf.length);
                        for (Room.Client client: room.getClients()) {
                            sentPacket.setSocketAddress(new InetSocketAddress(client.getIp()
                                    , client.getPort()));
                            ds.send(sentPacket);
                        }
                    }
                    break;
                default: break;
            }
        }

        private void processJoinRoom(JoinRoomRequestPacket packet, String fromIP, int fromPort) {
            Room room = roomMap.get(packet.getRoomId());
            JoinRoomResponsePacket joinRoomResponsePacket =
                    new JoinRoomResponsePacket(packet.getRoomId(), false);
            joinRoomResponsePacket.setGameMd5(packet.getGameMd5());
            if (room != null && room.getClients().size() < 2
                    && room.getGameMd5().equals(packet.getGameMd5())) {
                if (room.getClients().size() == 1) {
                    boolean isRepeatJoin = room.getClients().get(0).getIp().equals(fromIP)
                            && room.getClients().get(0).getPort() == fromPort;
                    if (isRepeatJoin) {
                        System.out.println("不能进重复加入！");
                        return;
                    }
                }
                Room.Client client = new Room.Client(fromIP, fromPort);
                room.getClients().add(client);
                joinRoomResponsePacket.setMaster(packet.getUserId() == room.getMasterId());
                joinRoomResponsePacket.setCode(Configuration.SUCCESS);
                joinRoomResponsePacket.setMsg("");
            } else {
                joinRoomResponsePacket.setMsg("游戏房间不存在 【" + packet.getRoomId() + "】!");
            }
            byte[] data = getSentBytes(joinRoomResponsePacket);
            DatagramPacket responsePacket = new DatagramPacket(data, data.length);
            responsePacket.setSocketAddress(new InetSocketAddress(fromIP, fromPort));
            try {
                ds.send(responsePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processCreateRoom(CreateRoomRequestPacket packet,
                                       String fromIP, int fromPort) throws IOException {
            Room room = new Room(IdUtils.getRandomId(), packet.getUserId());
            // 搜索游戏是否存在..
            room.setGameName(packet.getGameName());
            room.setGameMd5(packet.getGameMd5());
            CreateRoomResponsePacket createRoomResponsePacket =
                    new CreateRoomResponsePacket(room.getId(), "创建成功!");
            createRoomResponsePacket.setGameMd5(packet.getGameMd5());
            byte[] data = getSentBytes(createRoomResponsePacket);
            DatagramPacket responsePacket = new DatagramPacket(data, data.length);
            responsePacket.setSocketAddress(new InetSocketAddress(fromIP, fromPort));
            ds.send(responsePacket);
            roomMap.put(room.getId(), room);
        }

        private byte[] getSentBytes(Packet packet) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos)){
                objectOutputStream.writeObject(packet);
                return baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("待发送数据为空: " + packet);
        }
    }
}
