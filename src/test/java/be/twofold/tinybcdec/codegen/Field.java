package be.twofold.tinybcdec.codegen;

enum Field {
    // Extracted
    R, G, B, A, EP, SP,
    // Skipped
    M, PB, IB;

    int lane() {
        switch (this) {
            case B:
                return 0;
            case G:
                return 16;
            case R:
                return 32;
            case A:
                return 48;
            default:
                throw new AssertionError();
        }
    }
}
