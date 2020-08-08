package com.legend.network;

import com.legend.network.packet.Packet;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 数据包接收回调
 */
public interface ReceiveCallback {

    void onReceive(Packet packet);
}
