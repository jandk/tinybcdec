package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

class BCDecoderTest {

    @Test
    @Disabled
    void testBc4u() throws IOException {
        byte[] src = Arrays.copyOf(BCTestUtils.readResource("/bc4.dds"), 2048);

        byte[] actual = new BC4UDecoder().decode(64, 64, src, 0);
        byte[] expected = BCTestUtils.readPng("/bc4.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Disabled
    void testBc5u() throws IOException {
        byte[] src = Arrays.copyOf(BCTestUtils.readResource("/bc5.dds"), 65536);

        byte[] actual = new BC5UDecoder(3).decode(256, 256, src, 0);
        byte[] expected = BCTestUtils.readPng("/bc5.png");

        assertThat(actual).isEqualTo(expected);
    }

}
