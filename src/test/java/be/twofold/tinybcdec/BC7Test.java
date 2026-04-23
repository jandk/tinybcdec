package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC7Test {

    private final BlockDecoder decoder = BlockDecoder.bc7();

    @Test
    void testBC7() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc7.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc7.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC7InvalidBlock() {
        ByteBuffer src = ByteBuffer.allocate(16);
        ByteBuffer actual = decoder.decode(src, 4, 4);
        assertThat(actual).isEqualTo(ByteBuffer.allocate(16 * 4));
    }

}
