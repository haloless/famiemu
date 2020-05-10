package io.famiemu;

public interface AddressReader {

    byte readByte(int address);

    default int readU8(int address) {
        return Byte.toUnsignedInt(readByte(address));
    }

    default int readU16(int address) {
        int lo = readU8(address);
        int hi = readU8(address + 1);
        return lo | (hi << 8);
    }
}
