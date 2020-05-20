package com.legend.utils.disassemble;

/**
 * @author Legend
 * @data by on 20-5-18.
 * @description 内存的指令反汇编后的信息
 */
public class DecodeInfo {

    private Rule.IAddressingModeInfo addressingModeInfo;
    private String instruction;

    public DecodeInfo(Rule.IAddressingModeInfo addressingModeInfo, String instruction) {
        this.addressingModeInfo = addressingModeInfo;
        this.instruction = instruction;
    }

    public Rule.IAddressingModeInfo getAddressingModeInfo() {
        return addressingModeInfo;
    }

    public void setAddressingModeInfo(Rule.IAddressingModeInfo addressingModeInfo) {
        this.addressingModeInfo = addressingModeInfo;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }
}
