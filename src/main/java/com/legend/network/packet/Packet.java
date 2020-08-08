package com.legend.network.packet;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-8-7.
 * @description 数据包接口
 */
public interface Packet extends Serializable {
    int REQUEST_CREATE_ROOM = 0;
    int REQUEST_JOIN_ROOM = 1;
    int REQUEST_EXIT_ROOM = 2;
    int REQUEST_DISMISS_ROOM = 3;
    int REQUEST_INPUT_OPERATION = 4;

    int RESPONSE_CREATE_ROOM = 20;
    int RESPONSE_JOIN_ROOM = 21;
    int RESPONSE_EXIT_ROOM = 22;
    int RESPONSE_DISMISS_ROOM = 23;
    int RESPONSE_INPUT_OPERATION = 24;

    int getType();
}
