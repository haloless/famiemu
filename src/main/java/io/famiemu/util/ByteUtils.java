package io.famiemu.util;

import static java.lang.String.format;

public class ByteUtils {

    public static int uint(byte b) {
        return b & 0xFF;
    }

    public static int toAddress16(int lo, int hi) {
        return lo | (hi << 8);
    }

    public static StringBuilder formatHex$16(StringBuilder sb, int value) {
        sb.append(format("$%04X", value));
        return sb;
    }

    public static StringBuilder formatHex$8(StringBuilder sb, int value) {
        sb.append(format("$%02X", value));
        return sb;
    }

    public static StringBuilder formatHex16(StringBuilder sb, int value) {
        sb.append(format("%04X", value));
        return sb;
    }

    public static StringBuilder formatHex8(StringBuilder sb, int value) {
        sb.append(format("%02X", value));
        return sb;
    }


}
