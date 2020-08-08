package com.legend.network;

import com.legend.network.packet.Packet;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Legend
 * @data by on 20-8-6.
 * @description 任务处理中心
 */
public class ClientTaskHandler {

    private ExecutorService service = Executors.newFixedThreadPool(2);
    private DatagramSocket ds = null;
    private ReceiveCallback callback;

    public ClientTaskHandler() {
        try {
            ds = new DatagramSocket();
            new Thread(new TaskReceiver(ds)).start();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void send(Packet packet) {
        service.submit(() -> {
            byte[] data = getSentBytes(packet);
            DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
            datagramPacket.setSocketAddress(new InetSocketAddress(Configuration.SERVER_ADDRESS
                    , Configuration.SERVER_PORT));
            try {
                ds.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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

    public void setCallback(ReceiveCallback callback) {
        this.callback = callback;
    }

    private class TaskReceiver implements Runnable {

        private DatagramSocket ds = null;
        private byte[] data = new byte[1024];

        public TaskReceiver(DatagramSocket ds) {
            this.ds = ds;
        }

        @Override
        public void run() {
            while (ds != null && !ds.isClosed()) {
                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                try {
                    ds.receive(datagramPacket);
                    System.out.println("收到服务器下发的数据包: " + datagramPacket.getSocketAddress());
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Packet packet = (Packet) ois.readObject();
                    if (callback != null) {
                        callback.onReceive(packet);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static class Holder {
        public static ClientTaskHandler INSTANCE = new ClientTaskHandler();
    }

    public static ClientTaskHandler getInstance() {
        return Holder.INSTANCE;
    }
}
