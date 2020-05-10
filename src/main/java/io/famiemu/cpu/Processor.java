package io.famiemu.cpu;

import io.famiemu.Disassembly;
import io.famiemu.Emulator;

import java.io.PrintStream;

public class Processor {

    public enum Interruption {
        /** Interrupt Request */
        IRQ_BRK(0xFFFE, 0xFFFF),

        /** Non-Maskable Interrupt, on every V-Blank, NTSC 60 Hz */
        NMI(0xFFFA, 0xFFFB),

        /** Reset */
        RESET(0xFFFC, 0xFFFD),

        ;

        public final int vecLo, vecHi;

        Interruption(int vecLo, int vecHi) {
            this.vecLo = vecLo;
            this.vecHi = vecHi;
        }

        public int vector() {
            return vecLo;
        }

    }

    public static final int INT_NMI_VEC = 0xFFFA;
    public static final int INT_RESET_VEC = 0xFFFC;
    public static final int INT_IRQBRK_VEC = 0xFFFE;

    public static final int STACK_BASE = 0x100;

    /**
     * <h1>Processor Status Flags</h1>
     *
     * 0 = reset
     * 1 = set
     *
     * |7654|3210|
     * |NV_B|DIZC|
     * See https://wiki.nesdev.com/w/index.php/Status_flags
     */
    public static final class StatusFlag {
        /**
         * CARRY. Set if the add produced a carry, or if the subtraction produced a borrow.
         * Also holds bits after a logical shift.
         */
        public static final int CARRY = 0;
        /**
         * ZERO.  Set if the result of the last operation (load/inc/dec/add/sub) was zero.
         */
        public static final int ZERO = 1;
        /**
         * IRQ DISABLE.  Set if maskable interrupts are disabled.
         */
        public static final int IRQ_DISABLE = 2;
        /**
         * DECIMAL MODE. Set if decimal mode active.
         */
        public static final int DECIMAL_MODE = 3;
        /**
         * BRK COMMAND. Set if an interrupt caused by a BRK, reset if caused by an external interrupt.
         */
        public static final int BREAK_COMMAND = 4;
        /**
         * Not used. Always set to 1 when BRK=4 is to be set.
         */
        public static final int BREAK_CMD2 = 5;
        /**
         * OVERFLOW.
         * Set if the addition of two like-signed numbers or the subtraction of two unlike-signed numbers
         * produces a result greater than +127 or less than -128.
         */
        public static final int OVERFLOW = 6;
        /**
         * NEGATIVE. Set if bit 7 of the accumulator is set.
         */
        public static final int NEGATIVE = 7;


        // short alias
//        public static final int C = CARRY;
//        public static final int Z = ZERO;
//        public static final int I = IRQ_DISABLE;
//        public static final int D = DECIMAL_MODE;
//        public static final int B = BREAK_COMMAND;
//        public static final int B2 = BREAK_CMD2;
//        public static final int V = OVERFLOW;
//        public static final int N = NEGATIVE;
    }

    /** 16-bit program counter */
    public int programCounter;

    /** 8-bit stack pointer*/
    public int stackPointer;

    /** 8-bit accumulator A */
    public int accumulator;

    /** 8-bit index register X */
    public int xIndex;

    /** 8-bit index register Y */
    public int yIndex;

    /** 8-bit processor status */
    private int status;

    public int cycle;

    private Emulator emu;
    private Disassembly dis;

    private final PrintStream out = System.out;
    private final StringBuilder sb = new StringBuilder();

    public Processor() {}

    public Processor(Emulator emu) {
        this.emu = emu;
        this.dis = new Disassembly(emu);
    }

    public void clearStatus() {
        status = 0;
    }

    public void setFlag(int flag) {
        status |= (1 << flag);
    }

    public void clearFlag(int flag) {
//        processorStatus ^= (processorStatus & (1 << flag));
        status &= ~(1 << flag);
    }

    public int getFlag(int flag) {
        return (status >>> flag) & 1;
    }

    public boolean testFlag(int flag) {
        return getFlag(flag) != 0;
    }

    public void flipFlag(int flag) {
        status ^= (1 << flag);
    }

    public void updateFlag(int flag, int value) {
        if (value == 0)
            clearFlag(flag);
        else
            setFlag(flag);
    }

    /**
     * Set flag if condition=true.
     * Clear flag if condition=false.
     */
    public void updateFlag(int flag, boolean cond) {
        if (cond)
            setFlag(flag);
        else
            clearFlag(flag);
    }

    /**
     * Set CARRY if bit 0 of value is set.
     */
    private void updateCarryFlag(int value) {
        updateFlag(StatusFlag.CARRY, (value & 1) != 0);
    }

    /**
     * Set ZERO if value == 0.
     */
    private void updateZeroFlag(int value) {
        updateFlag(StatusFlag.ZERO, value == 0);
    }

    /**
     * Set OVERFLOW if bit 6 of value is set.
     */
    private void updateOverflowFlag(int value) {
        updateFlag(StatusFlag.OVERFLOW, (value & 0x40) != 0);
    }

    /**
     * Set NEGATIVE if bit 7 of value is set.
     */
    private void updateNegativeFlag(int value) {
        updateFlag(StatusFlag.NEGATIVE, (value & 0x80) != 0);
    }

    private void updateZeroNegativeFlag(int value) {
        updateZeroFlag(value);
        updateNegativeFlag(value);
    }


    public String getStatusString() {
        StringBuilder s = new StringBuilder();
        for (int i=7; i>=0; i--) {
            s.append(getFlag(i));
        }
        return s.toString();
    }

    public void setEmulator(Emulator emu) {
        this.emu = emu;
    }

    public void reset() {
        programCounter = emu.readU16(Interruption.RESET.vector());
        accumulator = 0;
        xIndex = yIndex = 0;
        stackPointer = 0xFD; // 0xFF-2

        //
        clearStatus();
        // always set this unused flag
        setFlag(StatusFlag.BREAK_CMD2);
        setFlag(StatusFlag.IRQ_DISABLE);

        cycle = 0;
    }

    public void singleStep() {
        assert emu != null;

        sb.setLength(0);
        dis.disassembly(sb, programCounter);
        while (sb.length() < 40) sb.append(' ');
        dump(sb);
        System.out.println(sb.toString());

        // get current OP code
        final int code = emu.readU8(programCounter++);
        final OP op = OP.lookup(code);
        if (op == null) {
            sb.append(" Unsupported code=").append(code).append(" ");
            throw new IllegalStateException(sb.toString());
        }

        // load operand
        final AddressingMode mode = op.Mode();
        final int address = readTargetAddress(mode);

        //
        final Instruction inst = op.Inst();
        switch (inst) {
            case ADC: // TODO
                break;
            case AND:
                accumulator &= emu.readU8(address);
                updateZeroFlag(accumulator);
                updateNegativeFlag(accumulator);
                break;
            case ASL: // TODO
                break;
            case BCC:
                if (!testFlag(StatusFlag.CARRY)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case BCS:
                if (testFlag(StatusFlag.CARRY)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case BEQ:
                if (testFlag(StatusFlag.ZERO)) {
                    programCounter = address;
                    cycle += 1;
                }
                break;
            case BIT: { // Bit Test
                int value = emu.readU8(address);
                // memory value copied to N & V flags
                updateNegativeFlag(value);
                updateOverflowFlag(value);
                //
                updateZeroFlag(value & accumulator);
                break;
            }
            case BMI: // Branch if Minus
                if (testFlag(StatusFlag.NEGATIVE)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case BNE:
                if (!testFlag(StatusFlag.ZERO)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case BPL: // Branch if Positive
                if (!testFlag(StatusFlag.NEGATIVE)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case BRK:
                // TODO
                break;
            case BVC: // Branch if Overflow Clear
                if (!testFlag(StatusFlag.OVERFLOW)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case BVS: // Branch if Overflow Set
                if (testFlag(StatusFlag.OVERFLOW)) {
                    programCounter = address;
                    cycle += 1; // branch succeeds
                }
                break;
            case CLC:
                clearFlag(StatusFlag.CARRY);
                break;
            case CLD:
                clearFlag(StatusFlag.DECIMAL_MODE);
                break;
            case CLI:
                clearFlag(StatusFlag.IRQ_DISABLE);
                break;
            case CLV:
                clearFlag(StatusFlag.OVERFLOW);
                break;
            case CMP: {
                int value = emu.readU8(address);
                int diff = accumulator - value;
                updateFlag(StatusFlag.CARRY, diff >= 0);
                updateZeroNegativeFlag(diff);
                break;
            }
            case CPX:
                break;
            case CPY:
                break;
            case DEC:
                break;
            case DEX:
                xIndex--;
                updateZeroNegativeFlag(xIndex);
                break;
            case DEY:
                yIndex--;
                updateZeroNegativeFlag(yIndex);
                break;
            case EOR:
                break;
            case INC:
                break;
            case INX:
                xIndex++;
                updateZeroNegativeFlag(xIndex);
                break;
            case INY:
                yIndex++;
                updateZeroNegativeFlag(yIndex);
                break;
            case JMP:
                programCounter = address;
                break;
            case JSR:
                execJSR(address);
                break;
            case LDA:
                accumulator = emu.readU8(address);
                updateZeroNegativeFlag(accumulator);
                break;
            case LDX:
                xIndex = emu.readU8(address);
                updateZeroNegativeFlag(xIndex);
                break;
            case LDY:
                yIndex = emu.readU8(address);
                updateZeroNegativeFlag(yIndex);
                break;
            case LSR: { // Logical Shift Right
                int value = address < 0 ? accumulator : emu.readU8(address);
                updateCarryFlag(value);
                // shift right
                value = value >>> 1;
                if (address < 0) accumulator = value;
                else emu.writeByte(address, (byte) value);
                updateZeroNegativeFlag(value);
                break;
            }
            case NOP:
                // nothing
                break;
            case ORA:
                // TODO
                break;
            case PHA: // Push Accumulator
                push8(accumulator);
                break;
            case PHP: // Push Processor Status
                // special: always set B<4>=1 and B2<5>=1
                push8(status | (1 << StatusFlag.BREAK_COMMAND) | (1 << StatusFlag.BREAK_CMD2));
                break;
            case PLA: // Pull Accumulator
                accumulator = pop8();
                updateZeroNegativeFlag(accumulator);
                break;
            case PLP: // Pull Processor Status
                status = pop8();
                clearFlag(StatusFlag.BREAK_COMMAND);
                setFlag(StatusFlag.BREAK_CMD2);
                break;
            case ROL:
                break;
            case ROR:
                break;
            case RTI:
                break;
            case RTS: // Return from Subroutine
                programCounter = pop16();
                programCounter++; // have to increase PC
                break;
            case SBC:
                break;
            case SEC:
                setFlag(StatusFlag.CARRY);
                break;
            case SED:
                setFlag(StatusFlag.DECIMAL_MODE);
                break;
            case SEI:
                setFlag(StatusFlag.IRQ_DISABLE);
                break;
            case STA:
                emu.writeByte(address, (byte) accumulator);
                break;
            case STX:
                emu.writeByte(address, (byte) xIndex);
                break;
            case STY:
                emu.writeByte(address, (byte) yIndex);
                break;
            case TAX: // A -> X
                xIndex = accumulator;
                updateZeroNegativeFlag(xIndex);
                break;
            case TAY: // A -> Y
                yIndex = accumulator;
                updateZeroNegativeFlag(yIndex);
                break;
            case TSX: // SP -> X
                xIndex = stackPointer;
                updateZeroNegativeFlag(xIndex);
                break;
            case TXA: // X -> A
                accumulator = xIndex;
                updateZeroNegativeFlag(accumulator);
                break;
            case TXS: // X -> SP
                stackPointer = xIndex;
                // no need to update flag
                break;
            case TYA: // Y -> A
                accumulator = yIndex;
                updateZeroNegativeFlag(accumulator);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + inst);
        }


        // TODO cycle must consider page change
        cycle += op.Cycles();


    }

    private void execJSR(int target) {
        // push return point
        int pcRet = programCounter - 1;
        push16(pcRet);

        programCounter = target;
    }

    private int readTargetAddress(AddressingMode mode) {
        int address = -1;
        switch (mode) {
            case Accumulator:
                // no operand
                break;
            case Implied:
                // no operand
                break;
            case Immediate:
                // constant at current PC will be the immediate value
                address = programCounter++;
                break;
            case ZeroPage:
                // 8-bit address
                address = emu.readU8(programCounter++);
                break;
            case ZeroPageX:
                address = emu.readU8(programCounter++);
                address += xIndex;
                address &= 0xFF;
                break;
            case ZeroPageY:
                address = emu.readU8(programCounter++);
                address += yIndex;
                address &= 0xFF;
                break;
            case Absolute:
                // 16-bit address
                address = emu.readU16(programCounter);
                programCounter += 2;
                break;
            case AbsoluteX:
                address = emu.readU16(programCounter);
                programCounter += 2;
                address += xIndex;
                break;
            case AbsoluteY:
                address = emu.readU16(programCounter);
                programCounter += 2;
                address += yIndex;
                break;
            case Indirect: {
                // first read the 16-bit indirect address at PC
                int tmp = emu.readU16(programCounter);
                programCounter += 2;
                // 6502 CPU bug: JMP ($xxFF)
                // if indirect address begins at the last byte of page
                // the high byte is taken from beginning of this page, not next page
                // e.g. JMP ($10FF)
                // should read $10FF and $1100
                // actually read $10FF and $1000
                int page = tmp & 0xFF00;
                int lo = emu.readU8(tmp);
                int hi = emu.readU8(page | ((tmp + 1) & 0x00FF));
                address = lo | (hi << 8);
                break;
            }
            case IndirectX: {
                int tmp = emu.readU8(programCounter++);
                tmp += xIndex;
                // note address wraps in zero page
                int lo = emu.readU8(tmp & 0xFF);
                int hi = emu.readU8((tmp + 1) & 0xFF);
                address = lo | (hi << 8);
                break;
            }
            case IndirectY: {
                int tmp = emu.readU8(programCounter++);
                address = emu.readU16(tmp);
                address += yIndex;
                break;
            }
            case Relative: {
                int offset = emu.readByte(programCounter++);
                address = programCounter + offset;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + mode);
        }

        return address;
    }

    private void push8(int b) {
        emu.setMainRam(STACK_BASE + stackPointer, (byte)b);
        stackPointer--;
    }

    private int pop8() {
        byte b = emu.getMainRam(STACK_BASE + stackPointer + 1);
        stackPointer++;
        return Byte.toUnsignedInt(b);
    }

    private void push16(int s) {
        // hi byte
        push8(s >>> 8);
        // lo byte
        push8(s & 0xFF);
    }

    private int pop16() {
        int lo = pop8();
        int hi = pop8();
        return lo | (hi << 8);
    }

    public StringBuilder dump(StringBuilder sb) {
        sb.append(String.format("A:%02X X:%02X Y:%02X P:%02X SP:%02X CYC:%d",
                accumulator, xIndex, yIndex, status, stackPointer, cycle));
        return sb;
    }
}
