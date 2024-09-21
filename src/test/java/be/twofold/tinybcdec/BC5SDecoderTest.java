package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5SDecoderTest {

    @Test
    void testBC5S() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5s.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5Signed, PixelOrder.BGR)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5s.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(actual[i + 2]).isEqualTo(expected[i + 2]);
            assertThat(actual[i + 1]).isEqualTo(expected[i + 1]);
            assertThat(actual[i + 0]).isZero();
        }
    }

    @Test
    void testBC5SNormalized() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5s.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5SignedNormalized, PixelOrder.BGR)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5s_normalized.png");

        assertThat(actual).isEqualTo(expected);
    }
}
