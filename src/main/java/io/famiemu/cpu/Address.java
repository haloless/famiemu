package io.famiemu.cpu;

public abstract class Address {

    protected int address;

    public abstract int size();

    public int page() {
        return address & 0xFF00;
    }

    public static final class Address8 extends Address {

        @Override
        public int size() {
            return 1;
        }
    }

    public static final class Address16 extends Address {

        @Override
        public int size() {
            return 2;
        }
    }

}
