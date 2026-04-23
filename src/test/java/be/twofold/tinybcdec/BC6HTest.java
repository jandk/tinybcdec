package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC6HTest {

    @Test
    void testBC6H_UF16() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc6h_uf16.dds");

        BlockDecoder decoder = BlockDecoder.bc6h(false);
        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readDDSFP16("/bc6h_uf16_16.dds", 256, 256);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC6H_SF16() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc6h_sf16.dds");

        BlockDecoder decoder = BlockDecoder.bc6h(true);
        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readDDSFP16("/bc6h_sf16_16.dds", 256, 256);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC6HInvalidBlock() {
        ByteBuffer src = ByteBuffer.allocate(16);
        byte[] invalidModes = {0b10011, 0b10111, 0b11011, 0b11111};
        for (byte invalidMode : invalidModes) {
            src.put(0, invalidMode);
            BlockDecoder decoder = BlockDecoder.bc6h(false);
            ByteBuffer actual = decoder.decode(src, 4, 4);
            ByteBuffer expected = ByteBuffer.allocate(16 * 2 * 3);
            assertThat(actual).isEqualTo(expected);
        }
    }

}
