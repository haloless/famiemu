package io.famiemu.cpu;

public enum Operand {

    NA,

    /** Accumulator */
    A,

    /** X-Register */
    X,
    /** X-Register */
    Y,


    /**
     * 8-bit constant
     * # + numeric expr
     * e.g. #$0A
     */
    Const8(1),


    /**
     * 8-bit address
     * e.g. $00
     */
    Addr8(1),

    /**
     * 16-bit address
     * e.g. $1234
     */
    Addr16(2),

    /**
     * signed 8-bit offset (-128 to +127)
     * e.g. *+4 (offset +4, since PC will increment by 2, actually only skip 2-byte instruction)
     */
    Offset8(1),

    ;

    private final int _size;

    Operand() {
        this(0);
    }

    Operand(int size) {
        this._size = size;
    }

    public int size() {
        return _size;
    }
}
