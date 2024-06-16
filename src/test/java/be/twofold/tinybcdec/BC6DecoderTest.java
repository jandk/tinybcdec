package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC6DecoderTest {

    @Test
    void testBC6H_UF16() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc6h_uf16.dds");

        BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC6Unsigned, PixelOrder.RGBA);
        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readDDSFP16("/bc6h_uf16_out.dds", 256, 256);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC6H_SF16() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc6h_sf16.dds");

        BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC6Signed, PixelOrder.RGBA);
        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readDDSFP16("/bc6h_sf16_out.dds", 256, 256);

        assertThat(actual).isEqualTo(expected);
    }

}
