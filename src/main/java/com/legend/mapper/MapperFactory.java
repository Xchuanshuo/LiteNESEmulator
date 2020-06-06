package com.legend.mapper;

/**
 * @author Legend
 * @data by on 20-4-19.
 * @description 根据nes文件信息中mapper id创建对应的Mapper
 */
public class MapperFactory {

    public static Mapper createMapperFromId(int id) {
        System.out.println("Current Mapper Id: " + id);
        switch (id) {
            case 0:
                return new NROM();
            case 1: // 双截龙1 洛克人2...
                return new MMC1();
            case 2:
                return new UNROM();
            case 3: // 冒险岛1..
                return new CNROM();
            case 4: // 快打旋风、热血、冒险岛、神龟、双截龙系列...
                return new MMC3();
            case 163: // 水浒神兽、仙剑、口袋妖怪..南京科技的游戏
                return new Mapper163();
            default:
                throw new RuntimeException("The Mapper id [" + id + "] implementation is not exist!" );
        }
    }
}
