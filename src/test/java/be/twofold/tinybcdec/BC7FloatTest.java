package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC7FloatTest {

    private final BlockDecoder decoder = BlockDecoder.bc7Float();

    @Test
    void testBC7() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc7.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc7.png", true);

        BCTestUtils.assertBufferEqualsFloats(actual, expected);
    }

    @Test
    void testBC7InvalidBlock() {
        ByteBuffer src = ByteBuffer.allocate(16);
        ByteBuffer actual = decoder.decode(src, 4, 4);

        BCTestUtils.assertBufferEquals(actual, ByteBuffer.allocate(16 * BC7Float.BPP));
    }

    @Test
    void testMatchesByteDecoderExactly() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc7.dds");

        ByteBuffer bgra = BlockDecoder.bc7()
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer floats = decoder
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);

        for (int i = 0; i < 256 * 256; i++) {
            int packed = bgra.getInt(i * 4);
            assertThat(floats.getFloat(i * 16 /**/)).isEqualTo(((packed >>> 16) & 0xFF) / 255.0f);
            assertThat(floats.getFloat(i * 16 + +4)).isEqualTo(((packed >>> +8) & 0xFF) / 255.0f);
            assertThat(floats.getFloat(i * 16 + +8)).isEqualTo(((packed /*  */) & 0xFF) / 255.0f);
            assertThat(floats.getFloat(i * 16 + 12)).isEqualTo(((packed >>> 24) & 0xFF) / 255.0f);
        }
    }

}
