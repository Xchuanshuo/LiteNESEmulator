package com.legend.storage;

import java.io.IOException;

/**
 * @author Legend
 * @data by on 20-5-4.
 * @description 定义存储接口 给需要实现数据存储的与重载类自己实现
 */
public interface ISave {

    byte[] getSaveBytes() throws IOException;

    void reload(byte[] bytes) throws IOException ;
}
