package be.twofold.tinybcdec.codegen;

import java.util.*;
import java.util.stream.*;

final class BC6HLayout implements Layout {
    @SuppressWarnings("ConcatenationWithEmptyString")
    private static final String LAYOUT = "" +
        "M0 M0 M0 M0 M0 M0 M0 M0 M0 M0 M0 M0 M0 M0\n" +
        "M1 M1 M1 M1 M1 M1 M1 M1 M1 M1 M1 M1 M1 M1\n" +
        "G24 G25 M2 M2 M2 M2 M2 M2 M2 M2 M2 M2 M2 M2\n" +
        "B24 G34 M3 M3 M3 M3 M3 M3 M3 M3 M3 M3 M3 M3\n" +
        "B34 G35 M4 M4 M4 M4 M4 M4 M4 M4 M4 M4 M4 M4\n" +
        "R00 R00 R00 R00 R00 R00 R00 R00 R00 R00 R00 R00 R00 R00\n" +
        "R01 R01 R01 R01 R01 R01 R01 R01 R01 R01 R01 R01 R01 R01\n" +
        "R02 R02 R02 R02 R02 R02 R02 R02 R02 R02 R02 R02 R02 R02\n" +
        "R03 R03 R03 R03 R03 R03 R03 R03 R03 R03 R03 R03 R03 R03\n" +
        "R04 R04 R04 R04 R04 R04 R04 R04 R04 R04 R04 R04 R04 R04\n" +
        "R05 R05 R05 R05 R05 R05 R05 R05 R05 R05 R05 R05 R05 R05\n" +
        "R06 R06 R06 R06 R06 R06 R06 R06 R06 G34 R06 R06 R06 R06\n" +
        "R07 B30 R07 R07 R07 R07 R07 R07 R07 B30 R07 R07 R07 R07\n" +
        "R08 B31 R08 R08 R08 R08 G34 B30 B31 B31 R08 R08 R08 R08\n" +
        "R09 B24 R09 R09 R09 B24 B24 B24 B24 B24 R09 R09 R09 R09\n" +
        "G00 G00 G00 G00 G00 G00 G00 G00 G00 G00 G00 G00 G00 G00\n" +
        "G01 G01 G01 G01 G01 G01 G01 G01 G01 G01 G01 G01 G01 G01\n" +
        "G02 G02 G02 G02 G02 G02 G02 G02 G02 G02 G02 G02 G02 G02\n" +
        "G03 G03 G03 G03 G03 G03 G03 G03 G03 G03 G03 G03 G03 G03\n" +
        "G04 G04 G04 G04 G04 G04 G04 G04 G04 G04 G04 G04 G04 G04\n" +
        "G05 G05 G05 G05 G05 G05 G05 G05 G05 G05 G05 G05 G05 G05\n" +
        "G06 G06 G06 G06 G06 G06 G06 G06 G06 G25 G06 G06 G06 G06\n" +
        "G07 B25 G07 G07 G07 G07 G07 G07 G07 B25 G07 G07 G07 G07\n" +
        "G08 B32 G08 G08 G08 G08 B32 G25 B25 B32 G08 G08 G08 G08\n" +
        "G09 G24 G09 G09 G09 G24 G24 G24 G24 G24 G09 G09 G09 G09\n" +
        "B00 B00 B00 B00 B00 B00 B00 B00 B00 B00 B00 B00 B00 B00\n" +
        "B01 B01 B01 B01 B01 B01 B01 B01 B01 B01 B01 B01 B01 B01\n" +
        "B02 B02 B02 B02 B02 B02 B02 B02 B02 B02 B02 B02 B02 B02\n" +
        "B03 B03 B03 B03 B03 B03 B03 B03 B03 B03 B03 B03 B03 B03\n" +
        "B04 B04 B04 B04 B04 B04 B04 B04 B04 B04 B04 B04 B04 B04\n" +
        "B05 B05 B05 B05 B05 B05 B05 B05 B05 B05 B05 B05 B05 B05\n" +
        "B06 B06 B06 B06 B06 B06 B06 B06 B06 G35 B06 B06 B06 B06\n" +
        "B07 B33 B07 B07 B07 B07 B07 B07 B07 B33 B07 B07 B07 B07\n" +
        "B08 B35 B08 B08 B08 B08 B33 G35 B35 B35 B08 B08 B08 B08\n" +
        "B09 B34 B09 B09 B09 B34 B34 B34 B34 B34 B09 B09 B09 B09\n" +
        "R10 R10 R10 R10 R10 R10 R10 R10 R10 R10 R10 R10 R10 R10\n" +
        "R11 R11 R11 R11 R11 R11 R11 R11 R11 R11 R11 R11 R11 R11\n" +
        "R12 R12 R12 R12 R12 R12 R12 R12 R12 R12 R12 R12 R12 R12\n" +
        "R13 R13 R13 R13 R13 R13 R13 R13 R13 R13 R13 R13 R13 R13\n" +
        "R14 R14 R14 R010 R010 R14 R14 R14 R14 R14 R14 R14 R14 R015\n" +
        "G34 R15 R010 G34 B24 G34 R15 G34 G34 R15 R15 R15 R15 R014\n" +
        "G20 G20 G20 G20 G20 G20 G20 G20 G20 G20 R16 R16 R16 R013\n" +
        "G21 G21 G21 G21 G21 G21 G21 G21 G21 G21 R17 R17 R17 R012\n" +
        "G22 G22 G22 G22 G22 G22 G22 G22 G22 G22 R18 R18 R011 R011\n" +
        "G23 G23 G23 G23 G23 G23 G23 G23 G23 G23 R19 R010 R010 R010\n" +
        "G10 G10 G10 G10 G10 G10 G10 G10 G10 G10 G10 G10 G10 G10\n" +
        "G11 G11 G11 G11 G11 G11 G11 G11 G11 G11 G11 G11 G11 G11\n" +
        "G12 G12 G12 G12 G12 G12 G12 G12 G12 G12 G12 G12 G12 G12\n" +
        "G13 G13 G13 G13 G13 G13 G13 G13 G13 G13 G13 G13 G13 G13\n" +
        "G14 G14 G010 G14 G010 G14 G14 G14 G14 G14 G14 G14 G14 G015\n" +
        "B30 G15 B30 G010 B30 B30 B30 G15 B30 G15 G15 G15 G15 G014\n" +
        "G30 G30 G30 G30 G30 G30 G30 G30 G30 G30 G16 G16 G16 G013\n" +
        "G31 G31 G31 G31 G31 G31 G31 G31 G31 G31 G17 G17 G17 G012\n" +
        "G32 G32 G32 G32 G32 G32 G32 G32 G32 G32 G18 G18 G011 G011\n" +
        "G33 G33 G33 G33 G33 G33 G33 G33 G33 G33 G19 G010 G010 G010\n" +
        "B10 B10 B10 B10 B10 B10 B10 B10 B10 B10 B10 B10 B10 B10\n" +
        "B11 B11 B11 B11 B11 B11 B11 B11 B11 B11 B11 B11 B11 B11\n" +
        "B12 B12 B12 B12 B12 B12 B12 B12 B12 B12 B12 B12 B12 B12\n" +
        "B13 B13 B13 B13 B13 B13 B13 B13 B13 B13 B13 B13 B13 B13\n" +
        "B14 B14 B010 B010 B14 B14 B14 B14 B14 B14 B14 B14 B14 B015\n" +
        "B31 B15 B31 B31 B010 B31 B31 B31 B15 B15 B15 B15 B15 B014\n" +
        "B20 B20 B20 B20 B20 B20 B20 B20 B20 B20 B16 B16 B16 B013\n" +
        "B21 B21 B21 B21 B21 B21 B21 B21 B21 B21 B17 B17 B17 B012\n" +
        "B22 B22 B22 B22 B22 B22 B22 B22 B22 B22 B18 B18 B011 B011\n" +
        "B23 B23 B23 B23 B23 B23 B23 B23 B23 B23 B19 B010 B010 B010\n" +
        "R20 R20 R20 R20 R20 R20 R20 R20 R20 R20 IB0,00 IB0,00 IB0,00 IB0,00\n" +
        "R21 R21 R21 R21 R21 R21 R21 R21 R21 R21 IB0,01 IB0,01 IB0,01 IB0,01\n" +
        "R22 R22 R22 R22 R22 R22 R22 R22 R22 R22 IB0,02 IB0,02 IB0,02 IB0,02\n" +
        "R23 R23 R23 R23 R23 R23 R23 R23 R23 R23 IB1,00 IB1,00 IB1,00 IB1,00\n" +
        "R24 R24 R24 B30 B31 R24 R24 R24 R24 R24 IB1,01 IB1,01 IB1,01 IB1,01\n" +
        "B32 R25 B32 B32 B32 B32 R25 B32 B32 R25 IB1,02 IB1,02 IB1,02 IB1,02\n" +
        "R30 R30 R30 R30 R30 R30 R30 R30 R30 R30 IB1,03 IB1,03 IB1,03 IB1,03\n" +
        "R31 R31 R31 R31 R31 R31 R31 R31 R31 R31 IB2,00 IB2,00 IB2,00 IB2,00\n" +
        "R32 R32 R32 R32 R32 R32 R32 R32 R32 R32 IB2,01 IB2,01 IB2,01 IB2,01\n" +
        "R33 R33 R33 R33 R33 R33 R33 R33 R33 R33 IB2,02 IB2,02 IB2,02 IB2,02\n" +
        "R34 R34 R34 G24 B34 R34 R34 R34 R34 R34 IB2,03 IB2,03 IB2,03 IB2,03\n" +
        "B33 R35 B33 B33 B33 B33 R35 B33 B33 R35 IB3,00 IB3,00 IB3,00 IB3,00\n" +
        "PB0 PB0 PB0 PB0 PB0 PB0 PB0 PB0 PB0 PB0 IB3,01 IB3,01 IB3,01 IB3,01\n" +
        "PB1 PB1 PB1 PB1 PB1 PB1 PB1 PB1 PB1 PB1 IB3,02 IB3,02 IB3,02 IB3,02\n" +
        "PB2 PB2 PB2 PB2 PB2 PB2 PB2 PB2 PB2 PB2 IB3,03 IB3,03 IB3,03 IB3,03\n" +
        "PB3 PB3 PB3 PB3 PB3 PB3 PB3 PB3 PB3 PB3 IB0,10 IB0,10 IB0,10 IB0,10\n" +
        "PB4 PB4 PB4 PB4 PB4 PB4 PB4 PB4 PB4 PB4 IB0,11 IB0,11 IB0,11 IB0,11";

    static final int[] MODES = {0, 1, 2, 6, 10, 14, 18, 22, 26, 30, 3, 7, 11, 15};

    @Override
    public List<ModeDefinition> generate() {
        List<List<Bit>> parsed = LAYOUT.lines()
            .map(this::parseLine)
            .collect(Collectors.toList());

        return IntStream.range(0, parsed.get(0).size())
            .mapToObj(i -> generateMode(i, parsed))
            .collect(Collectors.toList());
    }

    private ModeDefinition generateMode(int modeIndex, List<List<Bit>> parsed) {
        List<Bit> bits = parsed.stream()
            .map(row -> row.get(modeIndex))
            .takeWhile(Objects::nonNull)
            .collect(Collectors.toList());

        return toRanges(MODES[modeIndex], bits);
    }

    // region Parsing

    private List<Bit> parseLine(String line) {
        return Arrays.stream(line.split("\\s+"))
            .map(this::parseBit)
            .collect(Collectors.toList());
    }

    private Bit parseBit(String s) {
        return parseTarget(s)
            .map(target -> {
                int shift = target.field() == Field.M
                    ? Integer.parseInt(s.substring(1))
                    : Integer.parseInt(s.substring(2));
                return new Bit(target, shift);
            })
            .orElse(null);
    }

    private Optional<Target> parseTarget(String s) {
        Field field = parseField(s);
        switch (field) {
            case IB:
            case PB:
                return Optional.empty();
            case M:
                return Optional.of(new Target(Field.M, 0));
            default:
                int index = Character.digit(s.charAt(1), 10);
                return Optional.of(new Target(field, index));
        }
    }

    private Field parseField(String s) {
        switch (s.charAt(0)) {
            case 'R':
                return Field.R;
            case 'G':
                return Field.G;
            case 'B':
                return Field.B;
            case 'M':
                return Field.M;
            default: {
                switch (s.substring(0, 2)) {
                    case "PB":
                        return Field.PB;
                    case "IB":
                        return Field.IB;
                    default:
                        throw new IllegalArgumentException("Invalid field: " + s);
                }
            }
        }
    }

    // endregion

    // region Combining

    private ModeDefinition toRanges(int mode, List<Bit> bits) {
        List<Range> ranges = new ArrayList<>();

        int start = 0;
        int direction = 0;
        for (int i = 1; i < bits.size(); i++) {
            Bit prev = bits.get(i - 1);
            Bit curr = bits.get(i);
            int diff = curr.shift() - prev.shift();

            boolean isContinuation = curr.target().equals(prev.target())
                && Math.abs(diff) == 1
                && (direction == 0 || direction == diff);

            if (isContinuation) {
                direction = diff;
                continue;
            }

            addRange(ranges, bits, start, i, direction);
            start = i;
            direction = 0;
        }

        addRange(ranges, bits, start, bits.size(), direction);
        return new ModeDefinition(mode, ranges, List.of());
    }

    private void addRange(List<Range> ranges, List<Bit> bits, int start, int end, int direction) {
        if (bits.get(start).target().field() == Field.M) {
            return;
        }
        if (direction < 0 && end - start == 2) {
            ranges.add(toRange(bits, start, start + 1, 0));
            ranges.add(toRange(bits, start + 1, end, 0));
        } else {
            ranges.add(toRange(bits, start, end, direction));
        }
    }

    private Range toRange(List<Bit> bits, int start, int end, int direction) {
        Bit startBit = bits.get(start);
        int count = end - start;
        return new Range(
            startBit.target(),
            direction < 0 ? startBit.shift() - (count - 1) : startBit.shift(),
            start,
            count,
            direction < 0
        );
    }

    // endregion

    static final class Bit {
        private final Target target;
        private final int shift;

        Bit(Target target, int shift) {
            this.target = target;
            this.shift = shift;
        }

        public Target target() {
            return target;
        }

        public int shift() {
            return shift;
        }

        @Override
        public String toString() {
            return "Bit(" +
                "target=" + target + ", " +
                "shift=" + shift +
                ")";
        }
    }
}
