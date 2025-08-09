package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class ConverterRawTest {
    private Converter<byte[]> converter;

    @BeforeEach
    void setUp() {
        converter = Converter.raw();
    }

    @Test
    void testConvert() {
        byte[] decoded = {1, 2, 3, 4, 5, 6};
        int width = 2;
        int height = 3;

        byte[] result1 = converter.convert(decoded, width, height, 1);
        byte[] result2 = converter.convert(decoded, width, height, 2);
        byte[] result3 = converter.convert(decoded, width, height, 3);
        byte[] result4 = converter.convert(decoded, width, height, 4);
        byte[] result6 = converter.convert(decoded, width, height, 6);

        assertThat(result1).isSameAs(decoded);
        assertThat(result2).isSameAs(decoded);
        assertThat(result3).isSameAs(decoded);
        assertThat(result4).isSameAs(decoded);
        assertThat(result6).isSameAs(decoded);
    }

    @Test
    void testConvertWithEmptyArray() {
        byte[] decoded = new byte[0];
        int width = 0;
        int height = 0;

        byte[] result = converter.convert(decoded, width, height, 1);

        assertThat(result).isSameAs(decoded);
        assertThat(result).isEmpty();
    }

    @Test
    void testConvertWithNullArray() {
        assertThatNullPointerException()
            .isThrownBy(() -> converter.convert(null, 1, 1, 1));
    }
}
