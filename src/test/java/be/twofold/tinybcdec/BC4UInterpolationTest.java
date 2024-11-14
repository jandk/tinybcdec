package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class BC4UInterpolationTest {
    @Test
    void testInterpolation() {
        for (int a0 = 0; a0 < 256; a0++) {
            for (int a1 = 0; a1 < 256; a1++) {
                float f0 = unpackUNorm8(a0);
                float f1 = unpackUNorm8(a1);

                assertThat((6 * a0 + 1 * a1 + 3) / 7).isEqualTo(packUNorm8(lerp(f0, f1, 1.0f / 7.0f)));
                assertThat((5 * a0 + 2 * a1 + 3) / 7).isEqualTo(packUNorm8(lerp(f0, f1, 2.0f / 7.0f)));
                assertThat((4 * a0 + 3 * a1 + 3) / 7).isEqualTo(packUNorm8(lerp(f0, f1, 3.0f / 7.0f)));
                assertThat((3 * a0 + 4 * a1 + 3) / 7).isEqualTo(packUNorm8(lerp(f0, f1, 4.0f / 7.0f)));
                assertThat((2 * a0 + 5 * a1 + 3) / 7).isEqualTo(packUNorm8(lerp(f0, f1, 5.0f / 7.0f)));
                assertThat((1 * a0 + 6 * a1 + 3) / 7).isEqualTo(packUNorm8(lerp(f0, f1, 6.0f / 7.0f)));

                assertThat((4 * a0 + 1 * a1 + 2) / 5).isEqualTo(packUNorm8(lerp(f0, f1, 1.0f / 5.0f)));
                assertThat((3 * a0 + 2 * a1 + 2) / 5).isEqualTo(packUNorm8(lerp(f0, f1, 2.0f / 5.0f)));
                assertThat((2 * a0 + 3 * a1 + 2) / 5).isEqualTo(packUNorm8(lerp(f0, f1, 3.0f / 5.0f)));
                assertThat((1 * a0 + 4 * a1 + 2) / 5).isEqualTo(packUNorm8(lerp(f0, f1, 4.0f / 5.0f)));
            }
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static int packUNorm8(float f) {
        return (int) (f * 255.0f + 0.5f);
    }

    private static float unpackUNorm8(int i) {
        return i / 255.0f;
    }
}
