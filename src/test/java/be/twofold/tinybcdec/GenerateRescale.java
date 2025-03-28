package be.twofold.tinybcdec;

final class GenerateRescale {
    public static void main(String[] args) {
        generateRescale(31, 255, false);
        generateRescale(63, 255, false);
        generateRescale(3 * 31, 255, false);
        generateRescale(3 * 63, 255, false);
        generateRescale(2 * 31, 255, false);
        generateRescale(2 * 63, 255, false);
        generateRescale(7 * 255, 255, false);
        generateRescale(5 * 255, 255, false);

        generateRescale(127, 255, true);
        generateRescale(7 * 127, 255, true);
        generateRescale(5 * 127, 255, true);

        // System.out.println(test(7 * 127, 255, 75193, 67108864, 19, true));
        // System.out.println(test(5 * 127, 255, 13159, 8388708, 16, true));
    }

    /**
     * Other approach:
     * We basically want to remap a certain range of numbers, to a different range of numbers
     * Let's say we want to do 5 -> 8 bit conversion. This would mean scaling from 1/31 to 1/255
     */

    private static void generateRescale(int srcMax, int dstMax, boolean signed) {
        for (int shift = 0; shift <= 20; shift++) {
            double factor = getFactor(srcMax, dstMax, signed);
            int mul = (int) ((1 << shift) * factor);
            int addMin = (int) (Math.round(getOffset(dstMax, signed) * (1 << shift)));
            int addMax = addMin + (1 << shift);

            // Sometimes +1 gives you a smaller constant solution
            for (int m = mul - 1; m <= mul + 2; m++) {
                for (int a = 0; a < addMax; a++) {
                    if (test(srcMax, dstMax, m, a, shift, signed)) {
                        printFunction(srcMax, m, a, shift, signed);
                        return;
                    }
                }
            }
        }
    }

    private static double getFactor(double srcMax, double dstMax, boolean signed) {
        return (dstMax / srcMax) * (signed ? 0.5 : 1.0);
    }

    private static double getOffset(int dstMax, boolean signed) {
        return signed ? dstMax * 0.5 : 0.0;
    }

    private static boolean test(int srcMax, int dstMax, int m, int a, int shift, boolean signed) {
        double factor = getFactor(srcMax, dstMax, signed);
        double offset = getOffset(dstMax, signed);
        for (int i = signed ? -srcMax : 0; i <= srcMax; i++) {
            int expected = (int) Math.round(i * factor + offset);
            int actual = (i * m + a) >> shift;

            if (expected != actual) {
                return false;
            }
        }
        return true;
    }

    private static void printFunction(int srcMax, int m, int a, int shift, boolean signed) {
        System.out.printf("private static byte scale%03d%s(int i) {%n", srcMax, signed ? "Signed" : "");
        System.out.printf("    return (byte) ((i * %d + %d) >>> %d);%n", m, a, shift);
        System.out.printf("}%n");
    }
}
