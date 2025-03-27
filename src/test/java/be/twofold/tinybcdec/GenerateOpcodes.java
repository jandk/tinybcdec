package be.twofold.tinybcdec;

import java.util.*;

public final class GenerateOpcodes {
    private static final Map<Character, Integer> COLORS = Map.of('R', 0, 'G', 1, 'B', 2);

    public static void main(String[] args) {
        var opcodes = "" +
            "0 M1..0, G24, B24, B34, R09..0, G09..0, B09..0, R14..0, G34, G23..0, G14..0, B30, G33..0, B14..0, B31, B23..0, R24..0, B32, R34..0, B33, PB4..0\n" +
            "1 M1..0, G25, G34, G35, R06..0, B30, B31, B24, G06..0, B25, B32, G24, B06..0, B33, B35, B34, R15..0, G23..0, G15..0, G33..0, B15..0, B23..0, R25..0, R35..0, PB4..0\n" +
            "2 M4..0, R09..0, G09..0, B09..0, R14..0, R010, G23..0, G13..0, G010, B30, G33..0, B13..0, B010, B31, B23..0, R24..0, B32, R34..0, B33, PB4..0\n" +
            "6 M4..0, R09..0, G09..0, B09..0, R13..0, R010, G34, G23..0, G14..0, G010, G33..0, B13..0, B010, B31, B23..0, R23..0, B30, B32, R33..0, G24, B33, PB4..0\n" +
            "10 M4..0, R09..0, G09..0, B09..0, R13..0, R010, B24, G23..0, G13..0, G010, B30, G33..0, B14..0, B010, B23..0, R23..0, B31, B32, R33..0, B34, B33, PB4..0\n" +
            "14 M4..0, R08..0, B24, G08..0, G24, B08..0, B34, R14..0, G34, G23..0, G14..0, B30, G33..0, B14..0, B31, B23..0, R24..0, B32, R34..0, B33, PB4..0\n" +
            "18 M4..0, R07..0, G34, B24, G07..0, B32, G24, B07..0, B33, B34, R15..0, G23..0, G14..0, B30, G33..0, B14..0, B31, B23..0, R25..0, R35..0, PB4..0\n" +
            "22 M4..0, R07..0, B30, B24, G07..0, G25, G24, B07..0, G35, B34, R14..0, G34, G23..0, G15..0, G33..0, B14..0, B31, B23..0, R24..0, B32, R34..0, B33, PB4..0\n" +
            "26 M4..0, R07..0, B31, B24, G07..0, B25, G24, B07..0, B35, B34, R14..0, G34, G23..0, G14..0, B30, G33..0, B15..0, B23..0, R24..0, B32, R34..0, B33, PB4..0\n" +
            "30 M4..0, R05..0, G34, B30, B31, B24, G05..0, G25, B25, B32, G24, B05..0, G35, B33, B35, B34, R15..0, G23..0, G15..0, G33..0, B15..0, B23..0, R25..0, R35..0, PB4..0\n" +
            "3 M4..0, R09..0, G09..0, B09..0, R19..0, G19..0, B19..0\n" +
            "7 M4..0, R09..0, G09..0, B09..0, R18..0, R010, G18..0, G010, B18..0, B010\n" +
            "11 M4..0, R09..0, G09..0, B09..0, R17..0, R010..11, G17..0, G010..11, B17..0, B010..11\n" +
            "15 M4..0, R09..0, G09..0, B09..0, R13..0, R010..15, G13..0, G010..15, B13..0, B010..15";

        var lines = opcodes.split("\\R");
        for (var line : lines) {
            for (var s : line.substring(line.indexOf(' ') + 1).split(", ")) {
                if (s.startsWith("M") || s.startsWith("PB")) {
                    continue;
                }
                var index = 3 * (s.charAt(1) - '0') + COLORS.get(s.charAt(0));

                var range = s.split("\\.\\.");
                var range1 = Integer.parseInt(range[0].substring(2));
                var range2 = range.length == 2 ? Integer.parseInt(range[1]) : range1;
                var shift = Math.min(range1, range2);
                var count = Math.max(range1, range2) - shift + 1;
                var reversed = range1 < range2 ? 1 : 0;

                System.out.printf("0x%04X, ", reversed << 12 | index << 8 | shift << 4 | count);
            }
            System.out.println();
        }
    }
}
