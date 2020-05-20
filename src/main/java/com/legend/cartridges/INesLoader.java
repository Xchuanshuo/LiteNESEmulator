package com.legend.cartridges;

import java.io.Serializable;

/**
 * @author Legend
 * @data by on 20-4-4.
 * @description NES加载器接口
 */
public interface INesLoader extends Serializable {

    int HORIZONTAL = 0;
    int VERTICAL = 1;
    int ONE_SCREEN_MIRRORING = 2;
    int FOUR_SCREEN_MIRRORING = 3;

    /**
     * 获取16KB PRG(程序)数据的页数
     * @return
     */
    int getPRGPageCount();

    /**
     * 获取8KB CHR(图像)数据的页数
     * @return
     */
    int getCHRPageCount();

    /**
     * 通过索引获取对应的PRG数据块
     * @param index
     * @return
     */
    byte[] getPRGPageByIndex(int index);

    /**
     * 通过索引获取对应的CHR数据块
     * @param index
     * @return
     */
    byte[] getCHRPageByIndex(int index);

    /**
     * 获取屏幕的方向
     * @return
     */
    int getMirroringDirection();

    /**
     * SRAM(游戏卡上的额外存储空间)是否可用
     * @return
     */
    boolean isSRAMEnable();


    /**
     * 设置SEAN是否可用
     * @param enable
     * @return
     */
    void setSRAMEnable(boolean enable);

    /**
     * 是否存在512字节的金手指
     * @return
     */
    boolean is512ByteTrainerPresent();

    /**
     * 金手指数据
     * @return
     */
    byte[] getTrainer();

    /**
     * 游戏是否是分4屏显示
     * @return
     */
    boolean isFourScreenMirroring();

    /**
     * 获取到nes文件对应的mapper(游戏卡上额外的内存映射器) id
     * @return
     */
    int getMapper();

    /**
     * 获取文件的md5值
     */
    String getFileMD5();
}
