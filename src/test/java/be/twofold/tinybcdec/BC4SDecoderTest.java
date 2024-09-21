package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC4SDecoderTest {

    private final BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC4Signed, PixelOrder.R);

    @Test
    void testBC4S() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4s.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4s.png");

        assertThat(actual).isEqualTo(expected);
    }

}
