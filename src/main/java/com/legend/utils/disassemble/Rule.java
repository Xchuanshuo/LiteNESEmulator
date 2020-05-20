package com.legend.utils.disassemble;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Legend
 * @data by on 20-4-9.
 * @description
 */
public class Rule {

    private static final String TYPE_ACCUMULATOR = "accumulator"; // 0
    private static final String TYPE_ABSOLUTE = "absolute"; // 1
    private static final String TYPE_ABSOLUTE_X = "absoluteX"; // 2
    private static final String TYPE_ABSOLUTE_Y = "absoluteY"; // 2
    private static final String TYPE_IMMEDIATE = "immediate"; // 1
    private static final String TYPE_IMPLIED = "implied"; // 0
    private static final String TYPE_INDIRECT = "indirect"; // 2
    private static final String TYPE_INDIRECT_X = "indirectX"; // 1
    private static final String TYPE_INDIRECT_Y = "indirectY"; // 1
    private static final String TYPE_ZERO_PAGE = "zeroPage"; // 1
    private static final String TYPE_ZERO_PAGE_X = "zeroPageX"; // 1
    private static final String TYPE_ZERO_PAGE_Y = "zeroPageY"; // 1
    private static final String TYPE_RELATIVE = "relative"; // 1

    private static Map<String, IAddressingModeInfo> addressingModeMap = new HashMap<>();

    public static final String[][] RULES = {
            {"BRK", TYPE_IMPLIED},
            {"ORA", TYPE_INDIRECT_X},
            {"STP", TYPE_IMPLIED},
            {"SLO", TYPE_INDIRECT_X},
            {"NOP", TYPE_ZERO_PAGE},
            {"ORA", TYPE_ZERO_PAGE},
            {"ASL", TYPE_ZERO_PAGE},
            {"SLO", TYPE_ZERO_PAGE},
            {"PHP", TYPE_IMPLIED},
            {"ORA", TYPE_IMMEDIATE},
            {"ASL", TYPE_ACCUMULATOR},
            {"ANC", TYPE_IMMEDIATE},
            {"NOP", TYPE_ABSOLUTE},
            {"ORA", TYPE_ABSOLUTE},
            {"ASL", TYPE_ABSOLUTE},
            {"SLO", TYPE_ABSOLUTE},
            {"BPL", TYPE_RELATIVE},
            {"ORA", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"SLO", TYPE_INDIRECT_Y},
            {"NOP", TYPE_ZERO_PAGE_X},
            {"ORA", TYPE_ZERO_PAGE_X},
            {"ASL", TYPE_ZERO_PAGE_X},
            {"SLO", TYPE_ZERO_PAGE_X},
            {"CLC", TYPE_IMPLIED},
            {"ORA", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_IMPLIED},
            {"SLO", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_ABSOLUTE_X},
            {"ORA", TYPE_ABSOLUTE_X},
            {"ASL", TYPE_ABSOLUTE_X},
            {"SLO", TYPE_ABSOLUTE_X},
            {"JSR", TYPE_ABSOLUTE},
            {"AND", TYPE_INDIRECT_X},
            {"STP", TYPE_IMPLIED},
            {"RLA", TYPE_INDIRECT_X},
            {"BIT", TYPE_ZERO_PAGE},
            {"AND", TYPE_ZERO_PAGE},
            {"ROL", TYPE_ZERO_PAGE},
            {"RLA", TYPE_ZERO_PAGE},
            {"PLP", TYPE_IMPLIED},
            {"AND", TYPE_IMMEDIATE},
            {"ROL", TYPE_IMPLIED},
            {"ANC", TYPE_IMMEDIATE},
            {"BIT", TYPE_ABSOLUTE},
            {"AND", TYPE_ABSOLUTE},
            {"ROL", TYPE_ABSOLUTE},
            {"RLA", TYPE_ABSOLUTE},
            {"BMI", TYPE_RELATIVE},
            {"AND", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"RLA", TYPE_INDIRECT_Y},
            {"NOP", TYPE_ZERO_PAGE_X},
            {"AND", TYPE_ZERO_PAGE_X},
            {"ROL", TYPE_ZERO_PAGE_X},
            {"RLA", TYPE_ZERO_PAGE_X},
            {"SEC", TYPE_IMPLIED},
            {"AND", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_IMPLIED},
            {"RLA", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_ABSOLUTE_X},
            {"AND", TYPE_ABSOLUTE_X},
            {"ROL", TYPE_ABSOLUTE_X},
            {"RLA", TYPE_ABSOLUTE_X},
            {"RTI", TYPE_IMPLIED},
            {"EOR", TYPE_INDIRECT_X},
            {"STP", TYPE_IMPLIED},
            {"SRE", TYPE_INDIRECT_X},
            {"NOP", TYPE_ZERO_PAGE},
            {"EOR", TYPE_ZERO_PAGE},
            {"LSR", TYPE_ZERO_PAGE},
            {"SRE", TYPE_ZERO_PAGE},
            {"PHA", TYPE_ACCUMULATOR},
            {"EOR", TYPE_IMMEDIATE},
            {"LSR", TYPE_IMPLIED},
            {"ALR", TYPE_IMMEDIATE},
            {"JMP", TYPE_ABSOLUTE},
            {"EOR", TYPE_ABSOLUTE},
            {"LSR", TYPE_ABSOLUTE},
            {"SRE", TYPE_ABSOLUTE},
            {"BVC", TYPE_RELATIVE},
            {"EOR", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"SRE", TYPE_INDIRECT_Y},
            {"NOP", TYPE_ZERO_PAGE_X},
            {"EOR", TYPE_ZERO_PAGE_X},
            {"LSR", TYPE_ZERO_PAGE_X},
            {"SRE", TYPE_ZERO_PAGE_X},
            {"CLI", TYPE_IMPLIED},
            {"EOR", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_IMPLIED},
            {"SRE", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_ABSOLUTE_X},
            {"EOR", TYPE_ABSOLUTE_X},
            {"LSR", TYPE_ABSOLUTE_X},
            {"SRE", TYPE_ABSOLUTE_X},
            {"RTS", TYPE_IMPLIED},
            {"ADC", TYPE_INDIRECT_X},
            {"STP", TYPE_IMPLIED},
            {"RRA", TYPE_INDIRECT_X},
            {"NOP", TYPE_ZERO_PAGE},
            {"ADC", TYPE_ZERO_PAGE},
            {"ROR", TYPE_ZERO_PAGE},
            {"RRA", TYPE_ZERO_PAGE},
            {"PLA", TYPE_ACCUMULATOR},
            {"ADC", TYPE_IMMEDIATE},
            {"ROR", TYPE_IMPLIED},
            {"ARR", TYPE_IMMEDIATE},
            {"JMP", TYPE_INDIRECT},
            {"ADC", TYPE_ABSOLUTE},
            {"ROR", TYPE_ABSOLUTE},
            {"RRA", TYPE_ABSOLUTE},
            {"BVS", TYPE_RELATIVE},
            {"ADC", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"RRA", TYPE_INDIRECT_Y},
            {"NOP", TYPE_ZERO_PAGE_X},
            {"ADC", TYPE_ZERO_PAGE_X},
            {"ROR", TYPE_ZERO_PAGE_X},
            {"RRA", TYPE_ZERO_PAGE_X},
            {"SEI", TYPE_IMPLIED},
            {"ADC", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_IMPLIED},
            {"RRA", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_ABSOLUTE_X},
            {"ADC", TYPE_ABSOLUTE_X},
            {"ROR", TYPE_ABSOLUTE_X},
            {"RRA", TYPE_ABSOLUTE_X},
            {"NOP", TYPE_IMMEDIATE},
            {"STA", TYPE_INDIRECT_X},
            {"NOP", TYPE_IMMEDIATE},
            {"SAX", TYPE_INDIRECT_X},
            {"STY", TYPE_ZERO_PAGE},
            {"STA", TYPE_ZERO_PAGE},
            {"STX", TYPE_ZERO_PAGE},
            {"SAX", TYPE_ZERO_PAGE},
            {"DEY", TYPE_IMPLIED},
            {"NOP", TYPE_IMMEDIATE},
            {"TXA", TYPE_ACCUMULATOR},
            {"XAA", TYPE_IMMEDIATE},
            {"STY", TYPE_ABSOLUTE},
            {"STA", TYPE_ABSOLUTE},
            {"STX", TYPE_ABSOLUTE},
            {"SAX", TYPE_ABSOLUTE},
            {"BCC", TYPE_RELATIVE},
            {"STA", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"AHX", TYPE_INDIRECT_Y},
            {"STY", TYPE_ZERO_PAGE_X},
            {"STA", TYPE_ZERO_PAGE_X},
            {"STX", TYPE_ZERO_PAGE_Y},
            {"SAX", TYPE_ZERO_PAGE_Y},
            {"TYA", TYPE_ACCUMULATOR},
            {"STA", TYPE_ABSOLUTE_Y},
            {"TXS", TYPE_IMPLIED},
            {"TAS", TYPE_ABSOLUTE_Y},
            {"SHY", TYPE_ABSOLUTE_X},
            {"STA", TYPE_ABSOLUTE_X},
            {"SHX", TYPE_ABSOLUTE_Y},
            {"AHX", TYPE_ABSOLUTE_Y},
            {"LDY", TYPE_IMMEDIATE},
            {"LDA", TYPE_INDIRECT_X},
            {"LDX", TYPE_IMMEDIATE},
            {"LAX", TYPE_INDIRECT_X},
            {"LDY", TYPE_ZERO_PAGE},
            {"LDA", TYPE_ZERO_PAGE},
            {"LDX", TYPE_ZERO_PAGE},
            {"LAX", TYPE_ZERO_PAGE},
            {"TAY", TYPE_IMPLIED},
            {"LDA", TYPE_IMMEDIATE},
            {"TAX", TYPE_IMPLIED},
            {"LAX", TYPE_IMMEDIATE},
            {"LDY", TYPE_ABSOLUTE},
            {"LDA", TYPE_ABSOLUTE},
            {"LDX", TYPE_ABSOLUTE},
            {"LAX", TYPE_ABSOLUTE},
            {"BCS", TYPE_RELATIVE},
            {"LDA", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"LAX", TYPE_INDIRECT_Y},
            {"LDY", TYPE_ZERO_PAGE_X},
            {"LDA", TYPE_ZERO_PAGE_X},
            {"LDX", TYPE_ZERO_PAGE_Y},
            {"LAX", TYPE_ZERO_PAGE_Y},
            {"CLV", TYPE_IMPLIED},
            {"LDA", TYPE_ABSOLUTE_Y},
            {"TSX", TYPE_IMPLIED},
            {"LAS", TYPE_ABSOLUTE_Y},
            {"LDY", TYPE_ABSOLUTE_X},
            {"LDA", TYPE_ABSOLUTE_X},
            {"LDX", TYPE_ABSOLUTE_Y},
            {"LAX", TYPE_ABSOLUTE_Y},
            {"CPY", TYPE_IMMEDIATE},
            {"CMP", TYPE_INDIRECT_X},
            {"NOP", TYPE_IMMEDIATE},
            {"DCP", TYPE_INDIRECT_X},
            {"CPY", TYPE_ZERO_PAGE},
            {"CMP", TYPE_ZERO_PAGE},
            {"DEC", TYPE_ZERO_PAGE},
            {"DCP", TYPE_ZERO_PAGE},
            {"INY", TYPE_IMPLIED},
            {"CMP", TYPE_IMMEDIATE},
            {"DEX", TYPE_IMPLIED},
            {"AXS", TYPE_IMMEDIATE},
            {"CPY", TYPE_ABSOLUTE},
            {"CMP", TYPE_ABSOLUTE},
            {"DEC", TYPE_ABSOLUTE},
            {"DCP", TYPE_ABSOLUTE},
            {"BNE", TYPE_RELATIVE},
            {"CMP", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"DCP", TYPE_INDIRECT_Y},
            {"NOP", TYPE_ZERO_PAGE_X},
            {"CMP", TYPE_ZERO_PAGE_X},
            {"DEC", TYPE_ZERO_PAGE_X},
            {"DCP", TYPE_ZERO_PAGE_X},
            {"CLD", TYPE_IMPLIED},
            {"CMP", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_IMPLIED},
            {"DCP", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_ABSOLUTE_X},
            {"CMP", TYPE_ABSOLUTE_X},
            {"DEC", TYPE_ABSOLUTE_X},
            {"DCP", TYPE_ABSOLUTE_X},
            {"CPX", TYPE_IMMEDIATE},
            {"SBC", TYPE_INDIRECT_X},
            {"NOP", TYPE_IMMEDIATE},
            {"ISC", TYPE_INDIRECT_X},
            {"CPX", TYPE_ZERO_PAGE},
            {"SBC", TYPE_ZERO_PAGE},
            {"INC", TYPE_ZERO_PAGE},
            {"ISC", TYPE_ZERO_PAGE},
            {"INX", TYPE_IMPLIED},
            {"SBC", TYPE_IMMEDIATE},
            {"NOP", TYPE_IMPLIED},
            {"SBC", TYPE_IMMEDIATE},
            {"CPX", TYPE_ABSOLUTE},
            {"SBC", TYPE_ABSOLUTE},
            {"INC", TYPE_ABSOLUTE},
            {"ISC", TYPE_ABSOLUTE},
            {"BEQ", TYPE_RELATIVE},
            {"SBC", TYPE_INDIRECT_Y},
            {"STP", TYPE_IMPLIED},
            {"ISC", TYPE_INDIRECT_Y},
            {"NOP", TYPE_ZERO_PAGE_X},
            {"SBC", TYPE_ZERO_PAGE_X},
            {"INC", TYPE_ZERO_PAGE_X},
            {"ISC", TYPE_ZERO_PAGE_X},
            {"SED", TYPE_IMPLIED},
            {"SBC", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_IMPLIED},
            {"ISC", TYPE_ABSOLUTE_Y},
            {"NOP", TYPE_ABSOLUTE_X},
            {"SBC", TYPE_ABSOLUTE_X},
            {"INC", TYPE_ABSOLUTE_X},
            {"ISC", TYPE_ABSOLUTE_X}
    };

    static {
        addressingModeMap.put(TYPE_ACCUMULATOR, new Accumulator());
        addressingModeMap.put(TYPE_ABSOLUTE, new Absolute());
        addressingModeMap.put(TYPE_ABSOLUTE_X, new AbsoluteX());
        addressingModeMap.put(TYPE_ABSOLUTE_Y, new AbsoluteY());
        addressingModeMap.put(TYPE_IMMEDIATE, new Immediate());
        addressingModeMap.put(TYPE_IMPLIED, new Implied());
        addressingModeMap.put(TYPE_INDIRECT, new Indirect());
        addressingModeMap.put(TYPE_INDIRECT_X, new IndirectX());
        addressingModeMap.put(TYPE_INDIRECT_Y, new IndirectY());
        addressingModeMap.put(TYPE_ZERO_PAGE, new ZeroPage());
        addressingModeMap.put(TYPE_ZERO_PAGE_X, new ZeroPageX());
        addressingModeMap.put(TYPE_ZERO_PAGE_Y, new ZeroPageY());
        addressingModeMap.put(TYPE_RELATIVE, new Relative());
    }

    public static IAddressingModeInfo getAddressingMode(String type) {
        return addressingModeMap.get(type);
    }

    private static class Accumulator implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ACCUMULATOR;
        }

        @Override
        public String format() {
            return "%s";
        }

        @Override
        public int offset() {
            return 0;
        }
    }

    private static class Absolute implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ABSOLUTE;
        }

        @Override
        public String format() {
            return "%s %X";
        }

        @Override
        public int offset() {
            return 2;
        }
    }

    private static class AbsoluteX implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ABSOLUTE_X;
        }

        @Override
        public String format() {

            return "%s %04X,x";
        }

        @Override
        public int offset() {
            return 2;
        }
    }

    private static class AbsoluteY implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ABSOLUTE_Y;
        }

        @Override
        public String format() {
            return "%s %04X,y";
        }

        @Override
        public int offset() {
            return 2;
        }
    }

    private static class Immediate implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_IMMEDIATE;
        }

        @Override
        public String format() {
            return "%s #%X";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    private static class Implied implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_IMPLIED;
        }

        @Override
        public String format() {
            return "%s";
        }

        @Override
        public int offset() {
            return 0;
        }
    }

    private static class Indirect implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_INDIRECT;
        }

        @Override
        public String format() {
            return "%s %X";
        }

        @Override
        public int offset() {
            return 2;
        }
    }

    private static class IndirectX implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_INDIRECT_X;
        }

        @Override
        public String format() {
            return "%s (%X,x)";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    private static class IndirectY implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_INDIRECT_Y;
        }

        @Override
        public String format() {
            return "%s (%X),y";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    private static class ZeroPage implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ZERO_PAGE;
        }

        @Override
        public String format() {
            return "%s %X";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    private static class ZeroPageX implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ZERO_PAGE_X;
        }

        @Override
        public String format() {
            return "%s %X,x";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    private static class ZeroPageY implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_ZERO_PAGE_Y;
        }

        @Override
        public String format() {
            return "%s %X,y";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    private static class Relative implements IAddressingModeInfo {

        @Override
        public String type() {
            return TYPE_RELATIVE;
        }

        @Override
        public String format() {
            return "%s %X";
        }

        @Override
        public int offset() {
            return 1;
        }
    }

    public interface IAddressingModeInfo {
        // 寻址模式
        String type();
        // dump的字符串格式(需要拼接指令的字符串作为前一部分)
        String format();
        // 字节偏移(指令需要多少字节的参数)
        int offset();
    }
}
