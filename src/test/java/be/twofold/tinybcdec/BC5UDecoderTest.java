package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5UDecoderTest {

    private final BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC5Unsigned, PixelOrder.BGR);

    @Test
    void testBC5U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u.png");

        assertThat(actual).isEqualTo(expected);
    }

}
