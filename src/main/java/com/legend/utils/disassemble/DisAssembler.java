package com.legend.utils.disassemble;

import com.legend.cartridges.FileNesLoader;
import com.legend.memory.IMemory;
import com.legend.memory.ReadonlyMemory;
import com.legend.memory.StandardMemory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Legend
 * @data by on 20-4-9.
 * @description 反汇编工具类
 */
public class DisAssembler {

    public static void dumpByAddress(StandardMemory memory, int startAddress, int endAddress) {
        dumpByAddress(memory, startAddress, endAddress, null);
    }

    public static int dumpByAddress(StandardMemory memory, int startAddress, int endAddress,
                                     OutputStream outputStream) {
        if (startAddress > endAddress) {
            System.out.println("dump内存指令结束！");
            if (outputStream != null) {
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return startAddress;
        }
        DecodeInfo decodeInfo = getDecodeInfo(memory, startAddress);
        String data = decodeInfo.getInstruction();
        System.out.print(data);
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 继续dump偏移后的第一个内存位置
        return dumpByAddress(memory, startAddress + decodeInfo.getAddressingModeInfo()
                .offset() + 1, endAddress, outputStream);
    }

    public static void dump(StandardMemory memory, int startAddress, int endAddress,
                            OutputStream outputStream) {
        // 分成多次dump 防止递归层次太高导致堆栈溢出
        int offset = (endAddress - startAddress) / 20;
        while (offset > 1000) {
            offset = offset / 2;
        }
        while (startAddress < endAddress) {
            int end = startAddress + offset;
            startAddress = dumpByAddress(memory, startAddress, Math.min(end, endAddress), outputStream);
        }
    }

    public static void dump(StandardMemory memory, int startAddress, int endAddress) {
        dump(memory, startAddress, endAddress, null);
    }

    public static List<String> dumpByCount(IMemory memory, int startAddress, int count) {
        int c = 0;
        List<String> instructionList = new ArrayList<>();
        while (c < count) {
            DecodeInfo decodeInfo = getDecodeInfo(memory, startAddress);
            instructionList.add(decodeInfo.getInstruction());
            startAddress += decodeInfo.getAddressingModeInfo().offset() + 1;
            c++;
        }
        return instructionList;
    }

    public static void dumpMemoryNativeData(IMemory memory, int startAddress, int endAddress,
                                            OutputStream outputStream) {
        dumpMemoryNativeData(memory, 16, startAddress, endAddress, outputStream);
    }

    public static void dumpMemoryNativeData(IMemory memory, int stepLength,
                                            int startAddress, int endAddress,
                                            OutputStream outputStream) {
        // 分成多次dump 防止递归层次太高导致堆栈溢出
        while (startAddress < endAddress) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("0x%06X", startAddress));
            for (int i = 0;i < stepLength;i++) {
                sb.append(String.format(" %02X", memory.readByte( startAddress + i)));
            }
            sb.append("\n");
            startAddress += stepLength;
            try {
                outputStream.write(sb.toString().getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getMemoryNativeData(IMemory memory, int startAddress, int endAddress) {
        return getMemoryNativeData(memory, 16, startAddress, endAddress);
    }

    public static List<String> getMemoryNativeData(IMemory memory, int stepLength,
                                                   int startAddress, int endAddress) {
        List<String> dataList = new ArrayList<>();
        while (startAddress < endAddress) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("0x%06X", startAddress));
            for (int i = 0;i < stepLength;i++) {
                sb.append(String.format(" %02X", memory.readByte( startAddress + i)));
            }
            sb.append("\n");
            startAddress += stepLength;
            dataList.add(sb.toString());
        }
        return dataList;
    }


    private static DecodeInfo getDecodeInfo(IMemory memory, int startAddress) {
        int opcode = memory.readByte(startAddress);
        String[] rule = Rule.RULES[opcode];
        if (rule == null) {
            throw new RuntimeException("The opcode" + opcode + " is invalid!");
        }
        int value = 0;
        Rule.IAddressingModeInfo mode = Rule.getAddressingMode(rule[1]);
        if (mode.offset() == 1) {
            value = memory.readByte(startAddress + 1);
        }
        if (mode.offset() == 2) {
            value = memory.readByte(startAddress + 1)
                    | memory.readByte(startAddress + 2) << 8;
        }
        StringBuilder sb = new StringBuilder();
        // 地址及指令、操作数
        sb.append(String.format("0x%4X: ", startAddress));
        sb.append(String.format("%-16s", String.format(mode.format(), rule[0], value)));

        // 机器码
        sb.append(String.format("%02X", opcode));
        if (mode.offset() > 0) {
            sb.append(String.format(" %02X", memory.readByte(startAddress + 1)));
        }
        if (mode.offset() > 1) {
            sb.append(String.format(" %02X", memory.readByte(startAddress + 2)));
        }
        String data = sb.toString() + "\n";

        return new DecodeInfo(mode, data);
    }


    public static void main(String[] args) throws IOException {
        FileNesLoader loader = new FileNesLoader("/home/legend/Projects/IdeaProjects/2020/jnes-chaofan_ver/target/test-classes/game2.nes");
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0x8000, new ReadonlyMemory(loader.getPRGPageByIndex(0)));
        memory.setMemory(0xC000, new ReadonlyMemory(loader.getPRGPageByIndex(1)));
        FileOutputStream outputStream = new FileOutputStream("code.txt");
        dump(memory, 0x8000, 0xFFFE, outputStream);
    }
}
