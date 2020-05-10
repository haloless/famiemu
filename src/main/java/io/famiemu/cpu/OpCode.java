package io.famiemu.cpu;

import io.famiemu.AddressReader;

import java.util.logging.Logger;

public class OpCode {

    private static final Logger logger = Logger.getLogger("OPCODE");

    public OP op;
    public int arg;

    boolean valid() {
        return op != null;
    }


    public static OpCode parse(AddressReader reader, int address) {

        int code = reader.readU8(address);
        OP op = OP.lookup(code);
        if (op == null) {
            logger.warning("Unknown code=" + code + " at address=" + address);
            return null;
        }

        OpCode res = new OpCode();
        res.op = op;
        // move to next address
        address++;

        // based on addressing mode, determine the number of operands to read
        switch (op.Mode()) {
            case Accumulator:
            case Implied:
                // nothing
                break;

            case Immediate:
            case ZeroPage:
            case ZeroPageX:
            case ZeroPageY:
            case IndirectX:
            case IndirectY:
                // 8-bit const or address
                res.arg = reader.readU8(address);
                break;

            case Absolute:
            case AbsoluteX:
            case AbsoluteY:
            case Indirect:
                // 16-bit address
                res.arg = reader.readU16(address);
                break;

            case Relative:
                // 8-bit signed offset
                res.arg = reader.readByte(address);
                break;

            case Unknown:
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + res.op.Mode());
        }

        return res;
    }

}
