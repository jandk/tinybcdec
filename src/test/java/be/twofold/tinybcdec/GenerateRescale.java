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

        generateRescale(7 * 127, 127, true);
        generateRescale(5 * 127, 127, true);
    }

    /**
     * Other approach:
     * We basically want to remap a certain range of numbers, to a different range of numbers
     * Let's say we want to do 5 -> 8 bit conversion. This would mean scaling from 1/31 to 1/255
     */

    private static void generateRescale(int srcMax, int dstMax, boolean signed) {
        for (int shift = 0; shift <= 16; shift++) {
            float factor = (float) dstMax / (float) srcMax;
            int multiplicand = (int) ((1 << shift) * factor);
            int addend = 1 << shift;

            for (int m = multiplicand - 10; m <= multiplicand + 10; m++) {
                for (int a = 0; a < addend; a++) {
                    if (signed) {
                        if (testSigned(srcMax, factor, m, a, shift)) {
                            printFunction(srcMax, dstMax, m, a, shift, true);
                            return;
                        }
                    } else {
                        if (testUnsigned(srcMax, factor, m, a, shift)) {
                            printFunction(srcMax, dstMax, m, a, shift, false);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean testUnsigned(int srcMax, float factor, int m, int a, int shift) {
        for (int i = 0; i <= srcMax; i++) {
            int expected = (int) (i * factor + 0.5);
            int actual = (i * m + a) >> shift;

            if (expected != actual) {
                return false;
            }
        }
        return true;
    }

    private static boolean testSigned(int srcMax, float factor, int m, int a, int shift) {
        for (int i = -srcMax; i <= srcMax; i++) {
            int expected = Math.round(i * factor);
            int actual = (i * m + a) >> shift;

            if (expected != actual) {
                return false;
            }
        }
        return true;
    }

    private static void printFunction(int srcMax, int dstMax, int m, int a, int shift, boolean signed) {
        System.out.println("private static int rescale" + srcMax + "To" + dstMax + (signed ? "Signed" : "") + "(int i) {");
        System.out.println("    return (i * " + m + " + " + a + ") >> " + shift + ";");
        System.out.println("}");
    }
}
