package be.twofold.tinybcdec.codegen;

import java.util.*;

final class ExtractGenerator {
    public static void main(String[] args) {
        generateLayout(new BC6HLayout());
    }

    private static void generateLayout(Layout layout) {
        List<ModeDefinition> definitions = layout.generate();
        for (ModeDefinition definition : definitions) {
            System.out.println(generateMethod(definition));
            break;
        }
    }

    private static String generateMethod(ModeDefinition modeDefinition) {
        int mode = modeDefinition.mode();
        List<Range> ranges = new ArrayList<>(modeDefinition.ranges());
        var pBits = modeDefinition.pBits();
        Collections.sort(ranges);

        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append("private void getColors").append(modeDefinition.mode()).append("(long lo, long hi, int[] colors) {\n");
        int pBit = -1;
        int pName = -1;
        for (int i = 0; i < ranges.size(); ) {
            int index = ranges.get(i).target().arrayIndex();
            if (!pBits.isEmpty() && pBits.get(index / 4) != pBit) {
                pBit = pBits.get(index / 4);
                pName++;
                sb.append("    long p").append(pName).append(" = ").append(read(pBit, 1, 0)).append(";\n");
            }

            StringBuilder rhs = new StringBuilder();
            int j = i;
            while (j < ranges.size() && ranges.get(j).target().arrayIndex() == index) {
                if (j > i) {
                    rhs.append(" | ");
                }
                rhs.append(term(ranges.get(j)));
                j++;
            }
            if (!pBits.isEmpty()) {
                rhs.append(" | p").append(pName);
            }
            sb.append("    colors[").append(pad(index)).append("] = (int) (").append(rhs).append(");\n");
            i = j;
        }
        return sb.append("}\n").toString();
    }

    private static String term(Range f) {
        if (f.reversed()) {
            String rhs = "REVERSE_" + f.count() + "[(int) (" + read(f.start(), f.count(), 0) + ")]";
            return f.shift() > 0 ? rhs + " << " + f.shift() : rhs;
        }
        return read(f.start(), f.count(), f.shift());
    }

    private static String read(int pos, int count, int shift) {
        if (pos >= 64) {
            return extract("hi", pos - 64, count, shift);
        }
        if (pos + count <= 64) {
            return extract("lo", pos, count, shift);
        }

        int loCount = 64 - pos;
        int hiCount = count - loCount;

        String lhs = extract("lo", pos, loCount, shift);
        String rhs = extract("hi", 0, hiCount, loCount + shift);
        return lhs + " | " + rhs;
    }

    private static String extract(String f, int pos, int count, int shift) {
        int mask = ((1 << count) - 1) << shift;
        return shift(f, pos - shift) + " & " + mask(mask);
    }

    private static String shift(String src, int amount) {
        if (amount > 0) {
            return src + " >> " + pad(amount);
        }
        if (amount < 0) {
            return src + " << " + pad(-amount);
        }
        return src + " /* */";
    }

    private static String mask(int mask) {
        return String.format("0x%03X", mask);
    }

    private static String pad(int value) {
        return value < 10 ? "+" + value : Integer.toString(value);
    }
}
