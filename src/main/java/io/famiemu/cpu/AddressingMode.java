package io.famiemu.cpu;

import com.sun.scenario.effect.Offset;

import static io.famiemu.cpu.Operand.*;

/**
 * See http://obelisk.me.uk/6502/addressing.html
 */
public enum AddressingMode {

    /**
     * INST A
     */
    Accumulator, // 1

    /**
     * Implicitly
     *
     * INST
     */
    Implied, // 1

    /**
     * 8-bit constant '#$00'
     * e.g.
     * LDA #$0A ; load $0A into accumulator
     */
    Immediate(Const8), // 2

    /**
     * Zero Page absolute
     */
    ZeroPage(Addr8),
    /**
     * Zero Page X Indexed
     */
    ZeroPageX(Addr8),
    /**
     * Zero Page Y Indexed
     */
    ZeroPageY(Addr8),

    Absolute(Addr16), // 3
    /**
     * Absolute X Indexed
     */
    AbsoluteX(Addr16), // 3
    /**
     * Absolute Y Indexed
     */
    AbsoluteY(Addr16), // 3


    /**
     * Only used by JMP
     */
    Indirect(Addr16),

    /**
     * TODO
     * Pre-Indexed Indirect by X
     * or "Indexed Indirect"
     *
     * <pre>INST ($A, X)</pre>
     *
     * <p></p>
     * Indexed indirect addressing is normally used in conjunction with a table of address held on zero page.
     * The address of the table is taken from the instruction
     * and the X register added to it (with zero page wrap around)
     * to give the location of the least significant byte of the target address.
     */
    IndirectX(Addr8),

    /**
     * TODO
     * Post-Indexed indirect by Y
     * or "Indirect Indexed"
     *
     * <pre>INST ($A), Y</pre>
     *
     * <p></p>
     * Indirect indirect addressing is the most common indirection mode used on the 6502.
     * In instruction contains the zero page location of the least significant byte of 16 bit address.
     * The Y register is dynamically added to this value to generated the actual target address for operation.
     */
    IndirectY(Addr8),

    /**
     * Used by branch (e.g. BEQ, BNE).
     * 8-bit relative offset (-128~+127)
     * use compliment number for negative.
     */
    Relative(Offset8),


    Unknown,

    ;

    private final Operand[] operands;

    AddressingMode(Operand... operands) {
        this.operands = operands;
    }

    public Operand[] Operands() {
        return operands;
    }

    public int OperandsNum() {
        return operands.length;
    }

    public int OpBytes() {
        int n = 0;
        for (Operand operand : operands)
            n += operand.size();
        return n;
    }


}
