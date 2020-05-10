package io.famiemu.cpu;

import java.util.EnumSet;
import java.util.Set;

/**
 * See http://obelisk.me.uk/6502/reference.html
 */
public enum Instruction {

    /**
     * <h2>Add with Carry</h2>
     */
    ADC,
    /**
     * <h2>Logical And</h2>
     * Accumulator & Byte of Memory
     */
    AND,
    /**
     * <h2>Arithmetic Shift Left</h2>
     * shift one bit left
     */
    ASL,

    /**
     * <h2>Branch if Carry Clear</h2>
     */
    BCC,
    /**
     * <h2>Branch if Carry Set</h2>
     */
    BCS,
    /**
     * <h2>Branch if Equal</h2>
     */
    BEQ,
    /**
     * <h2>Bit Test</h2>
     */
    BIT,
    /**
     * <h2>Branch if Minus</h2>
     */
    BMI,
    /**
     * <h2>Branch if Not Equal</h2>
     */
    BNE,
    /**
     * <h2>Branch if Positive</h2>
     */
    BPL,
    /**
     * <h2>Forced Interrupt</h2>
     */
    BRK,
    /**
     * <h2>Branch if Overflow Clear</h2>
     */
    BVC,
    /**
     * <h2>Branch if Overflow Set</h2>
     */
    BVS,

    /**
     * <h2>Clear Carry Flag</h2>
     */
    CLC,
    /**
     * <h2>Clear Decimal Model</h2>
     */
    CLD,
    /**
     * <h2>Clear Interrupt Disable flag</h2>
     */
    CLI,
    /**
     * <h2>Clear Overflow Flag</h2>
     */
    CLV,
    /**
     * <h2>Compare</h2>
     * Compare accumulator with another memory.
     */
    CMP,
    /**
     * <h2>Compare X-Register</h2>
     */
    CPX,
    /**
     * <h2>Compare Y-Register</h2>
     */
    CPY,

    /**
     * <h2>Decrement Memory</h2>
     */
    DEC,
    /**
     * <h2>Decrement X-Register</h2>
     */
    DEX,
    /**
     * <h2>Decrement Y-Register</h2>
     */
    DEY,

    /**
     * <h2>Exclusive Or</h2>
     * Accumulator ^ Memory
     */
    EOR,

    /**
     * <h2>Increment Memory</h2>
     */
    INC,
    /**
     * <h2>Increment X-Register</h2>
     */
    INX,
    /**
     * <h2>Increment Y-Register</h2>
     */
    INY,

    /**
     * <h2>Jump</h2>
     */
    JMP,
    /**
     * <h2>Jump to Subroutine</h2>
     */
    JSR,

    /**
     * <h2>Load Accumulator</h2>
     * Load 1 byte of memory into accumulator and set zero and negative flags.
     */
    LDA,
    /**
     * <h2>Load X-Register</h2>
     */
    LDX,
    /**
     * <h2>Load Y-Register</h2>
     */
    LDY,
    /**
     * <h2>Logical Shift Right</h2>
     */
    LSR,

    /**
     * <h2>No Operation</h2>
     */
    NOP,

    /**
     * <h2>Logical Inclusive Or</h2>
     */
    ORA,

    /**
     * <h2>Accumulator push stack</h2>
     */
    PHA,
    /**
     * <h2>Push Processor Status</h2>
     */
    PHP,
    /**
     * <h2>Accumulator pop stack</h2>
     */
    PLA,
    /**
     * <h2>Pop Processor Status</h2>
     */
    PLP,

    /**
     * <h2>Rotate Left</h2>
     */
    ROL,
    /**
     * <h2>Rotate Right</h2>
     */
    ROR,
    /**
     * <h2>Return from Interrupt</h2>
     */
    RTI,
    /**
     * <h2>Return from Subroutine</h2>
     */
    RTS,

    /**
     * <h2>Subtract with Carry</h2>
     */
    SBC,
    /**
     * <h2>Set Carry Flag</h2>
     */
    SEC,
    /**
     * <h2>Set Decimal Flag</h2>
     */
    SED,
    /**
     * <h2>Set Interrupt Disable flag</h2>
     */
    SEI,
    /**
     * <h2>Store Accumulator into memory</h2>
     */
    STA,
    /**
     * <h2>Store X Register into memory</h2>
     */
    STX,
    /**
     * <h2>Store Y Register into memory</h2>
     */
    STY,

    /**
     * <h2>Transfer Accumulator to X</h2>
     */
    TAX,
    /**
     * <h2>Transfer Accumulator to Y</h2>
     */
    TAY,
    /**
     * <h2>Transfer Stack Pointer to X</h2>
     */
    TSX,
    /**
     * <h2>Transfer X to Accumulator</h2>
     */
    TXA,
    /**
     * <h2>Transfer X to Stack Pointer</h2>
     */
    TXS,
    /**
     * <h2>Transfer Y to Accumulator</h2>
     */
    TYA,

    //
    // unofficial op codes
    //

//    STP,
//    SLO,

    ;

    private final EnumSet<AddressingMode> modes = EnumSet.noneOf(AddressingMode.class);

    public EnumSet<AddressingMode> Modes() {
        return modes;
    }
}
