package com.legend.cpu;

import com.legend.memory.IMemory;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Legend
 * @data by on 20-4-6.
 * @description 默认的CPU实现
 * idea:
 *      opcode：http://nparker.llx.com/a2/opcodes.html
 */
@Slf4j
public class StandardCPU implements ICPU {


    private static final long serialVersionUID = -6281356477140561332L;
    private static final Random RAND = new Random();

    private long cycle = 0;
    private IMemory curMemory;
    private CPURegister register = new CPURegister();
    // 中断向量的地址
    public static final int[] VECTOR_NMI = new int[]{0xFFFA, 0xFFFB};
    public static final int[] VECTOR_RESET = new int[]{0xFFFC, 0xFFFD};
    public static final int[] VECTOR_IRQ_OR_BRK = new int[]{0xFFFE, 0xFFFF};
    private static final int RULE_TYPE_UNKNOWN = -1;

    private int pendingNMI = 0;

    private List<IRQGenerator> irqGenerators = new ArrayList<>();

    @Override
    public CPURegister getRegister() {
        return register;
    }

    @Override
    public void setRegister(CPURegister register) {
        this.register = register;
    }

    @Override
    public void setMemory(IMemory memory) {
        this.curMemory = memory;
    }

    @Override
    public IMemory getMemory() {
        return curMemory;
    }

    @Override
    public long execute() {
        if (checkIRQ()) {
            return cycle;
        }
        if (pendingNMI >= 2) {
            pendingNMI--;
        } else if (pendingNMI == 1){
            pendingNMI--;
            nmiImpl();
            return cycle;
        }
        int opcode = curMemory.readByte(register.getPC());
        increasePC();
        switch (opcode) {
            case 0:
                return brk();
            case 0x20:
                return jsr();
            case 0x40:
                return rti();
            case 0x60:
                return rts();
            case 0x8:
                return php();
            case 0x28:
                return plp();
            case 0x48:
                return pha();
            case 0x68:
                return pla();
            case 0x88:
                return dey();
            case 0x98:
                return tya();
            case 0xA8:
                return tay();
            case 0xC8:
                return iny();
            case 0xE8:
                return inx();
            case 0x8A:
                return txa();
            case 0x9A:
                return txs();
            case 0xAA:
                return tax();
            case 0xBA:
                return tsx();
            case 0xCA:
                return dex();
            case 0xEA:
                return nop();
            default: break;
        }
        int col = opcode & 0x1F;
        if (col == 0x10) return branch(opcode >> 5);
        if (col == 0x18) return status(opcode >> 5);
        // aaabbbcc 其中bbb位为操作码的寻址模式 aaa为操作码
        // 类型 cc表示满足某个规则的指令
        int opcodeType = opcode >> 5 & 7; // aaa
        int addressingMode = opcode >> 2 & 7; // bbb
        int ruleType = opcode & 3; // cc
        long result = RULE_TYPE_UNKNOWN;
        if (ruleType == 1) {
            result = rule1(opcodeType, addressingMode);
        } else if (ruleType == 2) {
            result = rule2(opcodeType, addressingMode);
        } else if (ruleType == 0) {
            result = rule3(opcodeType, addressingMode, opcode);
        }
        if (result == RULE_TYPE_UNKNOWN) {
            result = illegalOpcode(opcode, addressingMode);
        }
        if (result == RULE_TYPE_UNKNOWN) {
            throw new RuntimeException("The opcode [" + Integer.toHexString(opcode) + "] is invalid!");
        }
        return result;
    }

    private int[] opcodeLowBits = {0x07, 0x17, 0x03, 0x13, 0x0F, 0x1F, 0x1B};

    private long illegalOpcode(int opcode, int addressingMode) {
        for (int i = 0;i < opcodeLowBits.length;i++) {
            int val = opcode - opcodeLowBits[i];
            switch (val) {
                case 0:
                    return slo(addressingMode);
                case 0x20:
                    return rla(addressingMode);
                case 0x40:
                    return sre(addressingMode);
                case 0x60:
                    return rra(addressingMode);
                case 0xC0:
                    return dcp(addressingMode);
                case 0xE0:
                    return isc(addressingMode);
                default: break;
            }
        }
        switch (opcode) {
            case 0x87:  // SAX and LAX
            case 0x97:
            case 0x8F:
            case 0x83:
                return sax(addressingMode);
            case 0xA7:
            case 0xB7:
            case 0xAF:
            case 0xA3:
            case 0xB3:
                return lax(addressingMode);
            case 0x7: // Combined ALU-Opcodes
            case 0xF:
                return slo(addressingMode);
            case 0xFF:
                return isc(addressingMode);
            case 0x1A: // NUL/NOP and KIL/JAM/HLT
            case 0x3A:
            case 0x5A:
            case 0x7A:
            case 0xDA:
            case 0xFA:
                nop();
                break;
            case 0x80:
            case 0x82:
            case 0x89:
            case 0xC2:
            case 0xE2:
                increasePC(1);
                return nop();
            case 0x04:
            case 0x44:
            case 0x64:
                cycle += 1;
                increasePC(1);
                return nop();
            case 0x14:
            case 0x34:
            case 0x54:
            case 0x74:
            case 0xD4:
            case 0xF4:
                cycle += 2;
                increasePC(1);
                return nop();
            case 0x0C:
            case 0x1C:
            case 0x3C:
            case 0x5C:
            case 0x7C:
            case 0xDC:
            case 0xFC:
                cycle += 2;
                increasePC(2);
                return nop();
            case 0x0B:
                return andAsl(addressingMode);
            case 0x2B:
                return andRol(addressingMode);
            case 0x4B:
                return alr(addressingMode);
            case 0x6B:
                return arr(addressingMode);
            case 0xCB:
                return axs(addressingMode);
            case 0xEB:
                return sbcNop(addressingMode);
            case 0xBB:
                return las(addressingMode);
            default: break;
        }
        return RULE_TYPE_UNKNOWN;
    }

    private boolean checkIRQ() {
        if (register.isDisableInterrupt()) {
            return false;
        }
        for (IRQGenerator generator : irqGenerators) {
            if (generator.getIRQLevel()) {
                irq();
                return true;
            }
        }
        return false;
    }

    private long rule1(int opcodeType, int addressingMode) {
        switch (opcodeType) {
            case 0:
                return ora(addressingMode);
            case 1:
                return and(addressingMode);
            case 2:
                return xor(addressingMode);
            case 3:
                return adc(addressingMode);
            case 4:
                return sta(addressingMode);
            case 5:
                return lda(addressingMode);
            case 6:
                return cmp(addressingMode);
            case 7:
                return sbc(addressingMode);
            default: break;
        }
        return RULE_TYPE_UNKNOWN;
    }

    private long rule2(int opcodeType, int addressingMode) {
        switch (opcodeType) {
            case 0:
                return asl(addressingMode);
            case 1:
                return rol(addressingMode);
            case 2:
                return lsr(addressingMode);
            case 3:
                return ror(addressingMode);
            case 4:
                return stx(addressingMode);
            case 5:
                return ldx(addressingMode);
            case 6:
                return dec(addressingMode);
            case 7:
                return inc(addressingMode);
            default: break;
        }
        return RULE_TYPE_UNKNOWN;
    }

    private long rule3(int opcodeType, int addressingMode, int opcode) {
        switch (opcodeType) {
            case 1:
                return bit(addressingMode);
            case 2:
            case 3:
                return jmp(opcode);
            case 4:
                return sty(addressingMode);
            case 5:
                return ldy(addressingMode);
            case 6:
                return cpy(addressingMode);
            case 7:
                return cpx(addressingMode);
            default: break;
        }
        return RULE_TYPE_UNKNOWN;
    }

    @Override
    public long getCycle() {
        return cycle;
    }

    @Override
    public void increaseCycle(int value) {
        cycle += value;
    }

    @Override
    public void increasePC() {
        register.setPC(register.getPC() + 1);
    }

    @Override
    public void reset() {
        register.reset();
        register.setPC(curMemory.readByte(VECTOR_RESET[0]) |
                curMemory.readByte(VECTOR_RESET[1]) << 8);
        cycle = 0;
    }

    @Override
    public void powerUp() {
        reset();
    }

    @Override
    public void nmi() {
        if (RAND.nextBoolean()) {
            pendingNMI = 2;
        } else {
//            log.info("NMI-------");
            nmiImpl();
        }
    }

    private void nmiImpl() {
        register.clearBreak();
        push(register.getPC() >> 8);
        push(register.getPC());
        push(register.getFlags());
        int vector = curMemory.readByte(VECTOR_NMI[0]) | curMemory.readByte(VECTOR_NMI[1]) << 8;
        register.setPC(vector);
    }

    @Override
    public void irq() {
        register.clearBreak();
        push(register.getPC() >> 8);
        push(register.getPC());
        push(register.getFlags());
        int vector = curMemory.readByte(VECTOR_IRQ_OR_BRK[0])
                | curMemory.readByte(VECTOR_IRQ_OR_BRK[1]) << 8;
        register.setDisableInterrupt();
        register.setPC(vector);
    }

    @Override
    public void addIRQGenerator(IRQGenerator generator) {
        irqGenerators.add(generator);
    }

    private int immediate() {
        int arg = curMemory.readByte(register.getPC());
        increasePC();
        cycle += 2;
        return arg;
    }

    private int zeroPage(int arg) {
        cycle += 3;
        return arg;
    }

    private int zeroPageX(int arg) {
        cycle += 4;
        return (arg + register.getX()) & 0xFF;
    }

    private int zeroPageY(int arg) {
        cycle += 4;
        return (arg + register.getY()) & 0xFF;
    }

    private int absolute(int arg) {
        int arg2 = curMemory.readByte(register.getPC());
        increasePC();
        cycle += 4;
        return (arg2 << 8) | arg;
    }

    private int absoluteX(boolean isStore, int arg) {
        int arg2 = curMemory.readByte(register.getPC());
        cycle += 4;
        increasePC();
        cycle += 4;
        int address = ((arg2 << 8) | arg) + register.getX();
        if (isStore || (address >> 8) != arg2) {
            // 访问其它页或需要存储时+1
            cycle += 1;
        }
        return address;
    }

    private int absoluteY(boolean isStore, int arg) {
        int arg2 = curMemory.readByte(register.getPC());
        cycle += 4;
        increasePC();
        cycle += 4;
        int address = ((arg2 << 8) | arg) + register.getY();
        if (isStore || (address >> 8) != arg2) {
            // 访问其它页时需要+1
            cycle += 1;
        }
        return address;
    }

    private int indirectX(int arg) {
        cycle += 6;
        int tmp = (arg + register.getX()) & 0xFF;
        int add1 = curMemory.readByte(tmp);
        int add2 = curMemory.readByte(tmp + 1);
        return add1 | (add2 << 8);
    }

    private int indirectY(boolean isStore, int arg) {
        cycle += 5;
        int high = curMemory.readByte((arg + 1) & 0xFF);
        int address = (curMemory.readByte(arg) | (high << 8)) + register.getY();
        if (isStore || address >> 8 != high) {
            cycle += 1;
        }
        return address;
    }

    private int getValue1(int addressingMode, boolean isStore) {
        if (addressingMode == 2) {
            return immediate();
        }
        return curMemory.readByte(getAddress1(addressingMode, isStore));
    }

    private int getValue2(int addressingMode, boolean isStore) {
        if (addressingMode == 0) {
            return immediate();
        }
        return curMemory.readByte(getAddress2(addressingMode, isStore));
    }

    private int getValue3(int addressingMode, boolean isStore) {
        if (addressingMode == 0) {
            return immediate();
        }
        return curMemory.readByte(getAddress3(addressingMode, isStore));
    }

    // 对于所有的算术逻辑运算
    private int getAddress1(int addressingMode, boolean isStore) {
        int arg = curMemory.readByte(register.getPC());
        increasePC();
        switch (addressingMode) {
            case 0:
                return indirectX(arg);
            case 1:
                return zeroPage(arg);
            case 3:
                return absolute(arg);
            case 4:
                return indirectY(isStore, arg);
            case 5:
                return zeroPageX(arg);
            case 6:
                return absoluteY(isStore, arg);
            case 7:
                return absoluteX(isStore, arg);
            default:
                register.setPC(register.getPC() - 1);
                return 0;
        }
    }

    // 对于其它的一些指令
    private int getAddress2(int addressingMode, boolean isStore) {
        int arg = curMemory.readByte(register.getPC());
        increasePC();
        switch (addressingMode) {
            case 1:
                return zeroPage(arg);
            case 3:
                return absolute(arg);
            case 5:
                return zeroPageX(arg);
            case 7:
                return absoluteX(isStore, arg);
            default:
                register.setPC(register.getPC() - 1);
                return 0;
        }
    }

    // 对所有STX LDX等寄存器存储指令
    private int getAddress3(int addressingMode, boolean isStore) {
        int arg = curMemory.readByte(register.getPC());
        increasePC();
        switch (addressingMode) {
            case 1:
                return zeroPage(arg);
            case 3:
                return absolute(arg);
            case 5:
                return zeroPageY(arg);
            case 7:
                return absoluteY(isStore, arg);
            default:
                register.setPC(register.getPC() - 1);
                return 0;
        }
    }

    private void setZeroByValue(int v) {
        register.setZero((v & 0xFF) == 0);
    }

    private void setNegativeByValue(int v) {
        register.setNegative((v & 0x80) != 0);
    }

    private void setMemoryValue(int address, int v) {
        curMemory.writeByte(address, v & 0xFF);
    }

    // 给定值与A寄存器的值进行或运算
    private long ora(int addressingMode) {
        int value = getValue1(addressingMode, false);
        int v = register.getA() | value;
        setNegativeByValue(v);
        setZeroByValue(v);
        register.setA(v & 0xFF);
        return cycle;
    }

    // 给定值与A寄存器的值进行与运算
    private long and(int addressingMode) {
        int value = getValue1(addressingMode, false);
        int v = register.getA() & value;
        setNegativeByValue(v);
        setZeroByValue(v);
        register.setA(v);
        return cycle;
    }

    // ...异或运算
    private long xor(int addressingMode) {
        int value = getValue1(addressingMode, false);
        int v = register.getA() ^ value;
        setNegativeByValue(v);
        setZeroByValue(v);
        register.setA(v);
        return cycle;
    }

    // ..将给定值与A寄存器的值以及进位标志的值进行加法运算
    private long adc(int addressingMode) {
        int value = getValue1(addressingMode, false);
        int v = register.getA() + register.getCarry() + value;
        register.setCarry(v > 255);
        // -- --> + || ++ --> -
        register.setOverflow(((v & 0x80) == 0 && (register.getA() & 0x80) != 0 && (value & 0x80) != 0)
                            || ((v & 0x80) != 0 && (register.getA() & 0x80) == 0 && (value & 0x80) == 0));
        setNegativeByValue(v);
        setZeroByValue(v);
        register.setA(v);
        return cycle;
    }

    // 将A寄存器的值存储到内存
    private long sta(int addressingMode) {
        int address = getAddress1(addressingMode, true);
        setMemoryValue(address, register.getA());
        return cycle;
    }

    // 从内存中加载值到A寄存器
    private long lda(int addressingMode) {
        int value = getValue1(addressingMode, false);
        setNegativeByValue(value);
        setZeroByValue(value);
        register.setA(value);
        return cycle;
    }

    // 寄存器A的值与给定值进行比较运算
    private long cmp(int addressingMode) {
        int value = getValue1(addressingMode, false);
        int v = register.getA() - value;
        setNegativeByValue(v);
        setZeroByValue(v);
        register.setCarry(v >= 0);
        return cycle;
    }

    // 通过借位从A寄存器中减去内存值
    private long sbc(int addressingMode) {
        int value = getValue1(addressingMode, false);
        int v = register.getA() + register.getCarry() - 1 - value;
        setNegativeByValue(v);
        setZeroByValue(v);
        register.setCarry(v >= 0);
        register.setOverflow(((v & 0x80) == 0 && (register.getA() & 0x80) != 0 && (value & 0x80) == 0)
                || ((v & 0x80) != 0 && (register.getA() & 0x80) == 0 && (value & 0x80) != 0));
        register.setA(v);
        return cycle;
    }

    // 逻辑/算术 左移
    private long asl(int addressingMode) {
        cycle += 2;
        if (addressingMode == 2) {
            int val = register.getA() << 1;
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((val & 0x100) != 0);
            register.setA(val); // 将结果写回A
        } else {
            int address = getAddress2(addressingMode, true);
            int val = curMemory.readByte(address) << 1;
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((val & 0x100) != 0);
            setMemoryValue(address, val); // 将结果写回内存
        }
        return cycle;
    }

    // 带进位左旋转
    private long rol(int addressingMode) {
        cycle += 2;
        if (addressingMode == 2) {
            int val = (register.getA() << 1) | register.getCarry();
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((val & 0x100) != 0);
            register.setA(val);
        } else {
            int address = getAddress2(addressingMode, true);
            int val = (curMemory.readByte(address) << 1) | register.getCarry();
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((val & 0x100) != 0);
            setMemoryValue(address, val);
        }
        return cycle;
    }

    // 逻辑右移
    private long lsr(int addressingMode) {
        cycle += 2;
        if (addressingMode == 2) {
            int val = register.getA() >> 1;
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((register.getA() & 1) != 0);
            register.setA(val); // 将结果写回A
        } else {
            int address = getAddress2(addressingMode, true);
            int value = curMemory.readByte(address);
            int val = value >> 1;
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((value & 1) != 0);
            setMemoryValue(address, val); // 将结果写回内存
        }
        return cycle;
    }

    // 带进位右旋转
    private long ror(int addressingMode) {
        cycle += 2;
        if (addressingMode == 2) {
            int val = (register.getA() >> 1) | (register.getCarry() << 7);
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((register.getA() & 1) != 0);
            register.setA(val);
        } else {
            int address = getAddress2(addressingMode, true);
            int value = curMemory.readByte(address);
            int val = value >> 1 | (register.getCarry() << 7);
            setNegativeByValue(val);
            setZeroByValue(val);
            register.setCarry((value & 1) != 0);
            setMemoryValue(address, val);
        }
        return cycle;
    }

    // 将寄存器X的值存储到内存
    private long stx(int addressingMode) {
        int address = getAddress3(addressingMode, true);
        setMemoryValue(address, register.getX());
        return cycle;
    }

    // 将内存的值装载到X寄存器
    private long ldx(int addressingMode) {
        int value = getValue3(addressingMode, false);
        register.setX(value);
        setNegativeByValue(value);
        setZeroByValue(value);
        return cycle;
    }

    // 将某个内存值-1
    private long dec(int addressingMode) {
        cycle += 2;
        int address = getAddress2(addressingMode, true);
        int val = curMemory.readByte(address) - 1;
        setNegativeByValue(val);
        setZeroByValue(val);
        setMemoryValue(address, val);
        return cycle;
    }

    // 将某个内存值+1
    private long inc(int addressingMode) {
        cycle += 2;
        int address = getAddress2(addressingMode, true);
        int val = curMemory.readByte(address) + 1;
        setNegativeByValue(val);
        setZeroByValue(val);
        setMemoryValue(address, val);
        return cycle;
    }

    // TODO
    private long bit(int addressingMode) {
        int value = getValue2(addressingMode, false);
        int v = register.getA() & value;
        setNegativeByValue(value);
        register.setOverflow((value & 0x40) != 0);
        setZeroByValue(v);
//        register.setOverflow((value & 0x20) != 0);
//        setNegativeByValue(value & 0x40);
        return cycle;
    }

    // 跳转指令
    private long jmp(int opcode) {
        cycle += 3;
        int address = curMemory.readByte(register.getPC())
                | curMemory.readByte(register.getPC() + 1) << 8;
        if (opcode == 0x6C) {
            cycle += 2;
            // 硬件bug jmp ($0x10FF) 本应该读取0x10FF和0x1100的值　实际读取　0x10FF和0x1000的值
            address = curMemory.readByte(address) | curMemory.readByte(
                    (address & 0xFF00) | (address + 1) &0x00FF) << 8;
        }
        register.setPC(address);
        return cycle;
    }

    // 将寄存器Y的值存储到内存
    private long sty(int addressingMode) {
        int address = getAddress2(addressingMode, true);
        setMemoryValue(address, register.getY());
        return cycle;
    }

    // 将内存的值加载到寄存器
    private long ldy(int addressingMode) {
        int value = getValue2(addressingMode, false);
        setNegativeByValue(value);
        setZeroByValue(value);
        register.setY(value);
        return cycle;
    }

    // 将Y与给定内存的值进行比较 即Y-nn
    private long cpy(int addressingMode) {
        int value = getValue2(addressingMode, false);
        int val = register.getY() - value;
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setCarry(val >= 0);
        return cycle;
    }

    // 将X与给定内存的值进行比较 即Y-nn
    private long cpx(int addressingMode) {
        int value = getValue2(addressingMode, false);
        int val = register.getX() - value;
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setCarry(val >= 0);
        return cycle;
    }

    // 分支指令
    private long branch(int branchType) {
        cycle += 2;
        boolean condition = false;
        switch (branchType) {
            case 0: // BPL
                condition = !register.isNegative();
                break;
            case 1: // BMI
                condition = register.isNegative();
                break;
            case 2: // BVC
                condition = !register.isOverflow();
                break;
            case 3: // BVS
                condition = register.isOverflow();
                break;
            case 4: // BCC/BLT
                condition = !register.isCarry();
                break;
            case 5: // BCS/BGE
                condition = register.isCarry();
                break;
            case 6: // BNE/BZC
                condition = !register.isZero();
                break;
            case 7: // BEQ/BZS
                condition = register.isZero();
                break;
            default: break;
        }
        int offset = (byte)curMemory.readByte(register.getPC());
        increasePC();
        if (condition) {
            cycle += 1; // condition为true时钟+1
            int oldPC = register.getPC();
            register.setPC(oldPC + offset);
            if (oldPC >> 8 != register.getPC() >> 8) {
                // 跨页面访问时钟+1
                cycle += 1;
            }
        }
        return cycle;
    }

    // 改变flags寄存器状态指令
    private long status(int statusType) {
        cycle += 2;
        switch (statusType) {
            case 0: //CLC
                register.clearCarry();
                break;
            case 1: // SEC
                register.setCarry();
                break;
            case 2: // CLI
                register.clearDisableInterrupt();
                break;
            case 3: // SEI
                register.setDisableInterrupt();
                break;
            case 5: // CLV
                register.clearOverflow();
                break;
            case 6: // CLD
                register.clearDecimal();
                break;
            case 7: // SED
                register.setDecimal();
                break;
            default: break;
        }
        return cycle;
    }

    // 中断
    private long brk() {
        cycle += 7;
        register.setBreak();
        int nextAddress = register.getPC() + 1;
        // push下一条要执行的指令地址 从中断返回时使用
        push(nextAddress >> 8);
        push(nextAddress);
        // push状态寄存器
        push(register.getFlags());
        register.setDisableInterrupt();
        // 读取中断向量地址的值
        register.setPC(curMemory.readByte(VECTOR_IRQ_OR_BRK[0])
                | curMemory.readByte(VECTOR_IRQ_OR_BRK[1]) << 8);
        return cycle;
    }

    // 子程序调用 call
    private long jsr() {
        cycle += 6;
        int nextAddress = register.getPC() + 1;
        push(nextAddress >> 8);
        push(nextAddress);
        register.setPC(curMemory.readByte(register.getPC())
                | curMemory.readByte(register.getPC() + 1) << 8);
        return cycle;
    }

    // 从BRK/IRQ/NMI中断返回 reti
    private long rti() {
        cycle += 6;
        register.setFlags(pop());
        register.setBreak();
        int low = pop();
        register.setPC(low | (pop() << 8));
        return cycle;
    }

    // 从jsl(call)返回 ret
    private long rts() {
        cycle += 6;
        int low = pop();
        int returnAddress = low | (pop() << 8);
        register.setPC(returnAddress + 1);
        return cycle;
    }

    // push flags寄存器的值到内存
    private long php() {
        cycle += 3;
        push(register.getFlags());
        return cycle;
    }

    // pop 内存的值到flags寄存器
    private long plp() {
        cycle += 4;
        int val = pop();
        register.setFlags(val);
        return cycle;
    }

    // push A寄存器的值到内存
    private long pha() {
        cycle += 3;
        push(register.getA());
        return cycle;
    }

    // pop 内存的值到寄存器
    private long pla() {
        cycle += 4;
        int val = pop();
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setA(val);
        return cycle;
    }

    // 将Y寄存器的值-1
    private long dey() {
        cycle += 2;
        int val = register.getY() - 1;
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setY(val);
        return cycle;
    }

    // 将寄存器A的值赋值给寄存器Y
    private long tay() {
        cycle += 2;
        int val = register.getA();
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setY(val);
        return cycle;
    }

    // 将Y寄存器的值+1
    private long iny() {
        cycle += 2;
        int val = register.getY() + 1;
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setY(val);
        return cycle;
    }

    // 将X寄存器的值+1
    private long inx() {
        cycle += 2;
        int val = register.getX() + 1;
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setX(val);
        return cycle;
    }

    // 将X寄存器的值赋值给A寄存器
    private long txa() {
        cycle += 2;
        int val = register.getX();
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setA(val);
        return cycle;
    }

    // 将Y寄存器的值存储到寄存器A
    private long tya() {
        cycle += 2;
        int v = register.getY();
        setZeroByValue(v);
        setNegativeByValue(v);
        register.setA(v);
        return cycle;
    }

    // 将X寄存器的值赋值给SP寄存器
    private long txs() {
        cycle += 2;
        int val = register.getX();
//        setNegativeByValue(val);
//        setZeroByValue(val);
        register.setSP(val);
        return cycle;
    }

    // 将A寄存器的值赋值给X
    private long tax() {
        cycle += 2;
        int val = register.getA();
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setX(val);
        return cycle;
    }

    // 将SP寄存器的值赋值给X寄存器
    private long tsx() {
        cycle += 2;
        int val = register.getSP();
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setX(val);
        return cycle;
    }

    // 将X寄存器值-1
    private long dex() {
        cycle += 2;
        int val = register.getX() - 1;
        setNegativeByValue(val);
        setZeroByValue(val);
        register.setX(val);
        return cycle;
    }

    // 空轮转
    private long nop() {
        cycle += 2;
        return cycle;
    }

    private void increasePC(int l) {
        for (int i = 0;i < l;i++) {
            increasePC();
        }
    }

    private long sax(int addressingMode) {
        sta(addressingMode);
        stx(addressingMode);
        return cycle;
    }

    private long lax(int addressingMode) {
        lda(addressingMode);
        ldx(addressingMode);
        return cycle;
    }

    private long slo(int addressingMode) {
        asl(addressingMode);
        ora(addressingMode);
        return cycle;
    }

    private long rla(int addressingMode) {
        rol(addressingMode);
        and(addressingMode);
        return cycle;
    }

    private long sre(int addressingMode) {
        lsr(addressingMode);
        xor(addressingMode);
        return cycle;
    }

    private long rra(int addressingMode) {
        ror(addressingMode);
        adc(addressingMode);
        return cycle;
    }

    private long dcp(int addressingMode) {
        dec(addressingMode);
        cmp(addressingMode);
        return cycle;
    }

    private long isc(int addressingMode) {
        inc(addressingMode);
        sbc(addressingMode);
        return cycle;
    }

    private long andAsl(int addressingMode) {
        and(addressingMode);
        asl(addressingMode);
        cycle -= 2;
        return cycle;
    }

    private long andRol(int addressingMode) {
        and(addressingMode);
        rol(addressingMode);
        cycle -= 2;
        return cycle;
    }

    private long alr(int addressingMode) {
        and(addressingMode);
        lsr(addressingMode);
        cycle -= 2;
        return cycle;
    }

    private long arr(int addressingMode) {
        and(addressingMode);
        ror(addressingMode);
        cycle -= 2;
        return cycle;
    }

    private long axs(int addressingMode) {
        cmp(addressingMode);
        dex();
        cycle -= 2;
        return cycle;
    }

    private long sbcNop(int addressingMode) {
        sbc(addressingMode);
        nop();
        cycle -= 2;
        return cycle;
    }

    private long las(int addressingMode) {
        lda(addressingMode);
        tsx();
        return cycle;
    }

    private void push(int value) {
        setMemoryValue(0x100 | register.getSP(), value);
        register.setSP((register.getSP() - 1) & 0xFF);
    }

    private int pop() {
        register.setSP(register.getSP() + 1);
        return curMemory.readByte(0x100 | register.getSP());
    }
}
