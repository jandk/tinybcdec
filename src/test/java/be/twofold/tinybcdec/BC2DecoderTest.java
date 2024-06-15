package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC2DecoderTest {

    private final BCDecoder decoder = BCDecoder.create(BCFormat.BC2, BCOrder.ABGR);

    @Test
    void testBC2() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc2.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc2.png");

        assertThat(actual).isEqualTo(expected);
    }

}
