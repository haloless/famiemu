package io.famiemu.cpu;

import java.util.*;
import java.util.stream.Collectors;

import static io.famiemu.cpu.AddressingMode.*;
import static io.famiemu.cpu.Instruction.*;

/**
 * See http://obelisk.me.uk/6502/reference.html
 */
public class OP {

    private final int key;
    private final Instruction inst;
    private final AddressingMode mode;
    private final int cycles;

    private OP(int key, Instruction inst, AddressingMode mode, int cycles) {
        this.key = key;
        this.inst = inst;
        this.mode = mode;
        this.cycles = cycles;
    }

    public int Key() {
        return key;
    }

    public Instruction Inst() {
        return inst;
    }

    public AddressingMode Mode() {
        return mode;
    }

    public int Cycles() {
        return cycles;
    }

    /**
     * @return Instruction (1 Byte) + Operands
     */
    public int Bytes() {
        return mode.OpBytes() + 1;
    }

    private static final OP[] table = new OP[256];

    private static void addOp(int key, Instruction inst, AddressingMode mode, int cycles) {
        if (table[key] != null)
            throw new RuntimeException("Already registered key=" + key);

        table[key] = new OP(key, inst, mode, cycles);

        // also register allowed mode to instruction
        inst.Modes().add(mode);
    }

    static {

        //
        addOp(0x00, BRK, Implied, 7);
        addOp(0x01, ORA, IndirectX, 6);
        addOp(0x05, ORA, ZeroPage, 3);
        addOp(0x06, ASL, ZeroPage, 5);
        addOp(0x08, PHP, Implied, 3);
        addOp(0x09, ORA, Immediate, 2);
        addOp(0x0A, ASL, Accumulator, 2);
        addOp(0x0D, ORA, Absolute, 4);
        addOp(0x0E, ASL, Absolute, 6);

        addOp(0x10, BPL, Relative, 2);
        addOp(0x11, ORA, IndirectY, 5);
        addOp(0x15, ORA, ZeroPageX, 4);
        addOp(0x16, ASL, ZeroPageX, 6);
        addOp(0x18, CLC, Implied, 2);
        addOp(0x19, ORA, AbsoluteY, 4);
        addOp(0x1D, ORA, AbsoluteX, 4);
        addOp(0x1E, ASL, AbsoluteX, 7);

        addOp(0x20, JSR, Absolute, 6);
        addOp(0x21, AND, IndirectX, 6);
        addOp(0x24, BIT, ZeroPage, 3);
        addOp(0x25, AND, ZeroPage, 3);
        addOp(0x26, ROL, ZeroPage, 5);
        addOp(0x28, PLP, Implied, 4);
        addOp(0x29, AND, Immediate, 2);
        addOp(0x2A, ROL, Accumulator, 2);
        addOp(0x2C, BIT, Absolute, 4);
        addOp(0x2D, AND, Absolute, 4);
        addOp(0x2E, ROL, Absolute, 6);


        addOp(0x30, BMI, Relative, 2);
        addOp(0x31, AND, IndirectY, 5);
        addOp(0x35, AND, ZeroPageX, 4);
        addOp(0x36, ROL, ZeroPageX, 6);
        addOp(0x38, SEC, Implied, 2);
        addOp(0x39, AND, AbsoluteY, 4);
        addOp(0x3D, AND, AbsoluteX, 4);
        addOp(0x3E, ROL, AbsoluteX, 7);

        addOp(0x40, RTI, Implied, 6);
        addOp(0x41, EOR, IndirectX, 6);
        addOp(0x45, EOR, ZeroPage, 3);
        addOp(0x46, LSR, ZeroPage, 5);
        addOp(0x48, PHA, Implied, 3);
        addOp(0x49, EOR, Immediate, 2);
        addOp(0x4A, LSR, Accumulator, 2);
        addOp(0x4C, JMP, Absolute, 3);
        addOp(0x4D, EOR, Absolute, 4);
        addOp(0x4E, LSR, Absolute, 6);


        addOp(0x50, BVC, Relative, 2);
        addOp(0x51, EOR, IndirectY, 5);
        addOp(0x55, EOR, ZeroPageX, 4);
        addOp(0x56, LSR, ZeroPageX, 6);
        addOp(0x58, CLI, Implied, 2);
        addOp(0x59, EOR, AbsoluteY, 4);
        addOp(0x5D, EOR, AbsoluteX, 4);
        addOp(0x5E, LSR, AbsoluteX, 7);

        addOp(0x60, RTS, Implied, 6);
        addOp(0x61, ADC, IndirectX, 6);
        addOp(0x65, ADC, ZeroPage, 3);
        addOp(0x66, ROR, ZeroPage, 5);
        addOp(0x68, PLA, Implied, 4);
        addOp(0x69, ADC, Immediate, 2);
        addOp(0x6A, ROR, Accumulator, 2);
        addOp(0x6C, JMP, Indirect, 5);
        addOp(0x6D, ADC, Absolute, 4);
        addOp(0x6E, ROR, Absolute, 6);



        addOp(0x70, BVS, Relative, 2);
        addOp(0x71, ADC, IndirectY, 5);
        addOp(0x75, ADC, ZeroPageX, 4);
        addOp(0x76, ROR, ZeroPageX, 6);
        addOp(0x78, SEI, Implied, 2);
        addOp(0x79, ADC, AbsoluteY, 4);
        addOp(0x7D, ADC, AbsoluteX, 4);
        addOp(0x7E, ROR, AbsoluteX, 7);


        addOp(0x81, STA, IndirectX, 6);
        addOp(0x84, STY, ZeroPage, 3);
        addOp(0x85, STA, ZeroPage, 3);
        addOp(0x86, STX, ZeroPage, 3);
        addOp(0x88, DEY, Implied, 2);
        addOp(0x8A, TXA, Implied, 2);
        addOp(0x8C, STY, Absolute, 4);
        addOp(0x8D, STA, Absolute, 4);
        addOp(0x8E, STX, Absolute, 4);

        addOp(0x90, BCC, Relative, 2);
        addOp(0x91, STA, IndirectY, 6);
        addOp(0x94, STY, ZeroPageX, 4);
        addOp(0x95, STA, ZeroPageX, 4);
        addOp(0x96, STX, ZeroPageY, 4);
        addOp(0x98, TYA, Implied, 2);
        addOp(0x99, STA, AbsoluteY, 5);
        addOp(0x9A, TXS, Implied, 2);
        addOp(0x9D, STA, AbsoluteX, 5);

        addOp(0xA0, LDY, Immediate, 2);
        addOp(0xA1, LDA, IndirectX, 6);
        addOp(0xA2, LDX, Immediate, 2);
        addOp(0xA4, LDY, ZeroPage, 3);
        addOp(0xA5, LDA, ZeroPage, 3);
        addOp(0xA6, LDX, ZeroPage, 3);
        addOp(0xA8, TAY, Implied, 2);
        addOp(0xA9, LDA, Immediate, 2);
        addOp(0xAA, TAX, Implied, 2);
        addOp(0xAC, LDY, Absolute, 4);
        addOp(0xAD, LDA, Absolute, 4);
        addOp(0xAE, LDX, Absolute, 4);


        addOp(0xB0, BCS, Relative, 2);
        addOp(0xB1, LDA, IndirectY, 5);
        addOp(0xB4, LDY, ZeroPageX, 4);
        addOp(0xB5, LDA, ZeroPageX, 4);
        addOp(0xB6, LDX, ZeroPageY, 4);
        addOp(0xB8, CLV, Implied, 2);
        addOp(0xB9, LDA, AbsoluteY, 4);
        addOp(0xBA, TSX, Implied, 2);
        addOp(0xBC, LDY, AbsoluteX, 4);
        addOp(0xBD, LDA, AbsoluteX, 4);
        addOp(0xBE, LDX, AbsoluteY, 4);

        addOp(0xC0, CPY, Immediate, 2);
        addOp(0xC1, CMP, IndirectX, 6);
        addOp(0xC4, CPY, ZeroPage, 3);
        addOp(0xC5, CMP, ZeroPage, 3);
        addOp(0xC6, DEC, ZeroPage, 5);
        addOp(0xC8, INY, Implied, 2);
        addOp(0xC9, CMP, Immediate, 2);
        addOp(0xCA, DEX, Implied, 2);
        addOp(0xCC, CPY, Absolute, 4);
        addOp(0xCD, CMP, Absolute, 4);
        addOp(0xCE, DEC, Absolute, 6);

        addOp(0xD0, BNE, Relative, 2);
        addOp(0xD1, CMP, IndirectY, 5);
        addOp(0xD5, CMP, ZeroPageX, 4);
        addOp(0xD6, DEC, ZeroPageX, 6);
        addOp(0xD8, CLD, Implied, 2);
        addOp(0xD9, CMP, AbsoluteY, 4);
        addOp(0xDD, CMP, AbsoluteX, 4);
        addOp(0xDE, DEC, AbsoluteX, 7);


        addOp(0xE0, CPX, Immediate, 2);
        addOp(0xE1, SBC, IndirectX, 6);
        addOp(0xE4, CPX, ZeroPage, 3);
        addOp(0xE5, SBC, ZeroPage, 3);
        addOp(0xE6, INC, ZeroPage, 5);
        addOp(0xE8, INX, Implied, 2);
        addOp(0xE9, SBC, Immediate, 2);
        addOp(0xEA, NOP, Implied, 2);
        addOp(0xEC, CPX, Absolute, 4);
        addOp(0xED, SBC, Absolute, 4);
        addOp(0xEE, INC, Absolute, 6);

        addOp(0xF0, BEQ, Relative, 2);
        addOp(0xF1, SBC, IndirectY, 5);
        addOp(0xF5, SBC, ZeroPageX, 4);
        addOp(0xF6, INC, ZeroPageX, 6);
        addOp(0xF8, SED, Implied, 2);
        addOp(0xF9, SBC, AbsoluteY, 4);
        addOp(0xFD, SBC, AbsoluteX, 4);
        addOp(0xFE, INC, AbsoluteX, 7);

        // non-official

    }

    public static OP lookup(int key) {
        return table[key];
    }


    @Override
    public String toString() {
        return inst.name();
    }


    public static void showAll() {
        for (Instruction inst : Instruction.values()) {
            System.out.println(inst);
            for (OP code : Arrays.stream(table)
                    .filter(c -> c != null && c.Inst() == inst)
                    .sorted(Comparator.comparing(OP::Mode))
                    .collect(Collectors.toList())) {
                System.out.printf("%12s\t$%02x\t%d\t%d\n",
                        code.Mode(), code.Key(), code.Bytes(), code.Cycles());
            }
        }
    }
}
