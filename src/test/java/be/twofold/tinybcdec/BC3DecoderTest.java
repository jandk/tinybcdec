package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC3DecoderTest {

    private final BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC3, PixelOrder.ABGR);

    @Test
    void testBC3() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc3.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc3.png");

        assertThat(actual).isEqualTo(expected);
    }

}
