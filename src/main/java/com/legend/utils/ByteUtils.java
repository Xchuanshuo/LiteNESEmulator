package com.legend.utils;

/**
 * @author Legend
 * @data by on 20-4-4.
 * @description 字节操作工具类
 */
public class ByteUtils {

    /**
     * 获取字节b的第index位
     * @param b
     * @param index
     * @return
     */
    public static int getBit(byte b, int index) {
        check(index);
        return (b >> index) & 0x1;
    }

    public static int getBit(int b, int index) {
        return getBit((byte)b, index);
    }

    public static int getBitsByRange(int b, int start, int end) {
        return getBitsByRange((byte) b, start, end);
    }

    /**
     * 获取字节b start-end位范围内的值
     * @param b
     * @param start
     * @param end
     * @return
     */
    public static int getBitsByRange(byte b, int start, int end) {
        check(start);
        check(end);
        return (b >> start) & (0xFF >> (7 - (end - start)));
    }

//    public static int getBitsByRange(int b, int start, int end) {
//        return (b >> start) & (0xFF >> (7 - end));
//    }

    public static void check(int index) {
        if (index < 0 || index > 7) throw new RuntimeException("The index must be 0~7. " + index);
    }

}
