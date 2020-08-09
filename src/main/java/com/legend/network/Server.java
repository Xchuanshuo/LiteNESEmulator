package com.legend.network;

import com.legend.network.packet.Packet;
import com.legend.network.packet.request.*;
import com.legend.network.packet.response.CreateRoomResponsePacket;
import com.legend.network.packet.response.DismissRoomResponsePacket;
import com.legend.network.packet.response.ExitRoomResponsePacket;
import com.legend.network.packet.response.JoinRoomResponsePacket;
import com.legend.utils.IdUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
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
                    System.out.println("收到退出房间请求, " + fromIP + ":" + fromPort);
                    processExitRoom((ExitRoomRequestPacket) packet, fromIP, fromPort);
                    break;
                case REQUEST_DISMISS_ROOM:
                    System.out.println("收到解散房间请求, " + fromIP + ":" + fromPort);
                    processDismissRoom((DismissRoomRequestPacket) packet, fromIP, fromPort);
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
                Room.Client client = new Room.Client(packet.getUserId(), fromIP, fromPort);
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

        private void processExitRoom(ExitRoomRequestPacket packet, String fromIP, int fromPort) throws IOException {
            Room room = roomMap.get(packet.getRoomId());
            ExitRoomResponsePacket responsePacket = new ExitRoomResponsePacket(packet.getRoomId());
            if (room != null) {
                if (room.getClients().size() == 0) roomMap.remove(packet.getRoomId());
                Iterator<Room.Client> iterator = room.getClients().iterator();
                while (iterator.hasNext()) {
                    Room.Client client = iterator.next();
                    if (client.getUserId() == packet.getUserId()
                            && fromIP.equals(client.getIp())
                            && fromPort == client.getPort()) {
                        iterator.remove();
                        responsePacket.setMsg("退出房间成功!房间号【" + packet.getRoomId() + "】");
                        responsePacket.setCode(Configuration.SUCCESS);
                        break;
                    }
                }
            }
            if (responsePacket.getCode() == Configuration.FAILURE) {
                responsePacket.setMsg("退出房间失败!");
            }
            byte[] data = getSentBytes(responsePacket);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
            datagramPacket.setSocketAddress(new InetSocketAddress(fromIP, fromPort));
            ds.send(datagramPacket);
        }

        private void processDismissRoom(DismissRoomRequestPacket packet, String fromIP, int fromPort) throws IOException {
            Room room = roomMap.get(packet.getRoomId());
            DismissRoomResponsePacket responsePacket = new DismissRoomResponsePacket(packet.getRoomId());
            if (room != null && room.getMasterId() == packet.getUserId()) {
                responsePacket.setCode(Configuration.SUCCESS);
                responsePacket.setMsg("解散成功！");
                // 对所有客户端发起房间退出包
                ExitRoomResponsePacket errp = new ExitRoomResponsePacket(packet.getRoomId(),
                        Configuration.SUCCESS, "退出房间,【" + packet.getRoomId() + "】 房间已经解散！");
                byte[] data = getSentBytes(errp);
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                for (Room.Client client : room.getClients()) {
                    datagramPacket.setSocketAddress(new InetSocketAddress(client.getIp(), client.getPort()));
                    ds.send(datagramPacket);
                }
                roomMap.remove(room.getId());
            } else {
                responsePacket.setMsg("解散失败！");
            }
            byte[] data = getSentBytes(responsePacket);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
            datagramPacket.setSocketAddress(new InetSocketAddress(fromIP, fromPort));
            ds.send(datagramPacket);
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
