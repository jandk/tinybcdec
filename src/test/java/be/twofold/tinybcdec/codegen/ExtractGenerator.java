package be.twofold.tinybcdec.codegen;

final class ExtractGenerator {
    public static void main(String[] args) {
        var bc6 = new BC6HLayout();
        bc6.parseRanges();
    }
}
