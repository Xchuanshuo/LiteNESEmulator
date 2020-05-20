package com.legend.cpu;

import com.legend.memory.DefaultMemory;
import com.legend.memory.IMemory;
import com.legend.memory.StandardMemory;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Legend
 * @data by on 20-4-7.
 * @description
 */
public class StandardCPUTest {

    @Test
    public void testPowerUp() {
        ICPU cpu = new StandardCPU();
        byte[] data = new byte[]{0x00, (byte) 0x80};
        IMemory defaultMemory = new DefaultMemory(data);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, defaultMemory);
        cpu.setMemory(memory);

        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());
    }

    @Test
    public void testOra() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{0x09, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setA(0x10);
        cpu.execute();
        Assert.assertEquals("ora", 0x11, register.getA());
    }

    @Test
    public void testAnd() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{0x29, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setA(0x11);
        cpu.execute();
        Assert.assertEquals("and", 0x1, register.getA());
    }

    @Test
    public void testBEQ() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0xF0, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setZero(true);
        cpu.execute();
        Assert.assertEquals("BEQ", 0x8003, register.getPC());
    }

    @Test
    public void testBNE() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0xD0, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setZero(true);
        cpu.execute();
        Assert.assertEquals("BEQ", 0x8002, register.getPC());
    }

    @Test
    public void testSEI() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0x78, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        cpu.execute();
        Assert.assertTrue("SEI", register.isDisableInterrupt());
    }

    @Test
    public void testCLC() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0x18, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setCarry(true);
        cpu.execute();
        Assert.assertFalse("clc", register.isCarry());
    }

    @Test
    public void testASL() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0xA, (byte) 0x01};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setA(2);
        cpu.execute();
        Assert.assertEquals("ASL", 4, register.getA());
    }

    @Test
    public void testINC() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0xEE, (byte) 0x00, (byte) 0x90};
        byte[] data = new byte[]{21};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        IMemory dataMemory = new DefaultMemory(data);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        memory.setMemory(0x9000, dataMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        cpu.execute();
        Assert.assertEquals("INC", 22, cpu.getMemory().readByte(0x9000));
    }

    @Test
    public void testDEC() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0xDE, (byte) 0xFF, (byte) 0x8F}; // DEC nnnn,x
        byte[] data = new byte[]{21};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        IMemory dataMemory = new DefaultMemory(data);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        memory.setMemory(0x9000, dataMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setX(1);
        cpu.execute();
        Assert.assertEquals("DEC", 20, cpu.getMemory().readByte(0x9000));
    }

    @Test
    public void testJMP1() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0x4C, 0x00, (byte) 0x90}; // jmp nnnn
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);

        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        cpu.execute();
        Assert.assertEquals("DEC", 0x9000, register.getPC());
    }

    @Test
    public void testJMP2() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0x6C, (byte) 0xFF, 0x10}; // jmp (nnnn)
        byte[] data1 = new byte[]{0x00};
        byte[] data2 = new byte[]{0x70};
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        IMemory data1Memory = new DefaultMemory(data1);
        IMemory data2Memory = new DefaultMemory(data2);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        memory.setMemory(0x10FF, data1Memory);
        memory.setMemory(0x1000, data2Memory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        cpu.execute();
        Assert.assertEquals("DEC", 0x7000, register.getPC());
    }

    @Test
    public void testCPY1() {
        ICPU cpu = new StandardCPU();
        byte[] resetVector = new byte[]{0x00, (byte) 0x80};
        byte[] opcode = new byte[]{(byte) 0xC0, 0x1}; // cpy #nn
        IMemory resetMemory = new DefaultMemory(resetVector);
        IMemory instructionMemory = new DefaultMemory(opcode);
        StandardMemory memory = new StandardMemory(0x10000);
        memory.setMemory(0xFFFC, resetMemory);
        memory.setMemory(0x8000, instructionMemory);
        cpu.setMemory(memory);
        CPURegister register = cpu.getRegister();
        cpu.powerUp();
        Assert.assertEquals("Reset Vector", 0x8000, register.getPC());

        register.setY(10);
        cpu.execute();
        Assert.assertFalse("CPY", register.isZero());
        Assert.assertFalse("CPY", register.isNegative());
    }
}