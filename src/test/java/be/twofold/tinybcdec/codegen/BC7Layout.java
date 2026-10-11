package be.twofold.tinybcdec.codegen;

import java.util.*;
import java.util.stream.*;

final class BC7Layout implements Layout {
    @SuppressWarnings("ConcatenationWithEmptyString")
    private static final String LAYOUT = "" +
        // Mode NS PB RB ISB CB AB EPB SPB IB IB2
        "0 3 4 0 0 4 0 1 0 3 0\n" +
        "1 2 6 0 0 6 0 0 1 3 0\n" +
        "2 3 6 0 0 5 0 0 0 2 0\n" +
        "3 2 6 0 0 7 0 1 0 2 0\n" +
        "4 1 0 2 1 5 6 0 0 2 3\n" +
        "5 1 0 2 0 7 8 0 0 2 2\n" +
        "6 1 0 0 0 7 7 1 0 4 0\n" +
        "7 2 6 0 0 5 5 1 0 2 0";

    @Override
    public boolean unpack() {
        return true;
    }

    @Override
    public boolean packed() {
        return true;
    }

    @Override
    public List<ModeDefinition> generate() {
        return LAYOUT.lines()
            .map(this::generateMode)
            .collect(Collectors.toList());
    }

    private ModeDefinition generateMode(String line) {
        int[] p = Arrays.stream(line.trim().split("\\s+"))
            .mapToInt(Integer::parseInt)
            .toArray();

        int mode = p[0];
        int ns = p[1];
        int pb = p[2];
        int rb = p[3];
        int isb = p[4];
        int cb = p[5];
        int ab = p[6];
        int epb = p[7];
        int spb = p[8];

        int epCount = 2 * ns;
        int start = (mode + 1) + pb + rb + isb;
        int shift = epb + spb;
        int pBitStart = start + epCount * (3 * cb + ab);

        List<Field> fields = ab > 0
            ? List.of(Field.R, Field.G, Field.B, Field.A)
            : List.of(Field.R, Field.G, Field.B);

        List<Endpoint> endpoints = new ArrayList<>();
        for (Field field : fields) {
            int count = field == Field.A ? ab : cb;
            for (int ep = 0; ep < epCount; ep++) {
                Target target = new Target(field, ep);
                int pBit = shift != 0 ? pBitStart + (epb != 0 ? ep : ep / 2) : -1;
                Range range = new Range(target, shift, start, count, false);
                endpoints.add(new Endpoint(target, List.of(range), pBit, count + shift));
                start += count;
            }
        }

        if (ab == 0) {
            for (int ep = 0; ep < epCount; ep++) {
                endpoints.add(new Endpoint(new Target(Field.A, ep), List.of(), -1, 8));
            }
        }

        return new ModeDefinition(mode, endpoints);
    }
}
