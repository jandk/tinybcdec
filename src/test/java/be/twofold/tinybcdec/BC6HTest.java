package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC6HTest {

    @Test
    void testBC6H_UF16() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc6h_uf16.dds");

        BlockDecoder decoder = BlockDecoder.bc6h(Signedness.UNSIGNED);
        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readDDSFP16("/bc6h_uf16_16.dds", 256, 256);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC6H_SF16() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc6h_sf16.dds");

        BlockDecoder decoder = BlockDecoder.bc6h(Signedness.SIGNED);
        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readDDSFP16("/bc6h_sf16_16.dds", 256, 256);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC6HInvalidBlock() {
        byte[] src = new byte[16];
        byte[] invalidModes = {0b10011, 0b10111, 0b11011, 0b11111};
        for (byte invalidMode : invalidModes) {
            src[0] = invalidMode;
            BlockDecoder decoder = BlockDecoder.bc6h(Signedness.UNSIGNED);
            byte[] actual = decoder.decode(src, 0, 4, 4);
            byte[] expected = new byte[16 * 2 * 3];
            assertThat(actual).isEqualTo(expected);
        }
    }

}
