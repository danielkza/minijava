package frame.mips;

import frame.Access;

public class InFrame implements Access {
    int offset;

    InFrame(int offset) {
        this.offset = offset;
    }

    public String toString() {
        return Integer.toString(this.offset);
    }
}
