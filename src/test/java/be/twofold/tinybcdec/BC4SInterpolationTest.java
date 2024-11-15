package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class BC4SInterpolationTest {
    @Test
    void testInterpolation() {
        for (int a0 = -127; a0 < 127; a0++) {
            for (int a1 = -127; a1 < 127; a1++) {
                float f0 = unpackSNorm8(a0);
                float f1 = unpackSNorm8(a1);

                assertThat(rescale889To127Signed(6 * a0 + 1 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 1.0f / 7.0f)));
                assertThat(rescale889To127Signed(5 * a0 + 2 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 2.0f / 7.0f)));
                assertThat(rescale889To127Signed(4 * a0 + 3 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 3.0f / 7.0f)));
                assertThat(rescale889To127Signed(3 * a0 + 4 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 4.0f / 7.0f)));
                assertThat(rescale889To127Signed(2 * a0 + 5 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 5.0f / 7.0f)));
                assertThat(rescale889To127Signed(1 * a0 + 6 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 6.0f / 7.0f)));

                assertThat(rescale635To127Signed(4 * a0 + 1 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 1.0f / 5.0f)));
                assertThat(rescale635To127Signed(3 * a0 + 2 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 2.0f / 5.0f)));
                assertThat(rescale635To127Signed(2 * a0 + 3 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 3.0f / 5.0f)));
                assertThat(rescale635To127Signed(1 * a0 + 4 * a1)).isEqualTo(packSNorm8(lerp(f0, f1, 4.0f / 5.0f)));
            }
        }
    }

    private static int rescale889To127Signed(int i) {
        return (i * 585 + 2048) >> 12;
    }

    private static int rescale635To127Signed(int i) {
        return (i * 819 + 2048) >> 12;
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static int packSNorm8(float f) {
        return Math.round(Math.min(1.0f, Math.max(f, -1.0f)) * 127.0f);
    }

    private static float unpackSNorm8(int i) {
        return Math.max(i, -127) / 127.0f;
    }
}
