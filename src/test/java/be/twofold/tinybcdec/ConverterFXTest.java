package be.twofold.tinybcdec;

import javafx.scene.image.*;
import org.junit.jupiter.api.*;

import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class ConverterFXTest {

    private Converter<Image> converter;

    @BeforeEach
    void setUp() {
        converter = Converter.fx(false);
    }

    @Test
    void testConvertStride1() {
        byte[] decoded = {0x00, 0x10, 0x20, 0x30, 0x40, 0x50};
        int width = 3;
        int height = 2;

        Image result = converter.convert(decoded, width, height, 1);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);

        PixelReader reader = result.getPixelReader();
        assertThat(reader.getArgb(0, 0)).isEqualTo(0xFF000000);
        assertThat(reader.getArgb(1, 0)).isEqualTo(0xFF101010);
        assertThat(reader.getArgb(2, 0)).isEqualTo(0xFF202020);
        assertThat(reader.getArgb(0, 1)).isEqualTo(0xFF303030);
        assertThat(reader.getArgb(1, 1)).isEqualTo(0xFF404040);
        assertThat(reader.getArgb(2, 1)).isEqualTo(0xFF505050);
    }

    @Test
    void testConvertStride2() {
        byte[] decoded = {0, 0, -1, 0, 0, -1, -1, -1};
        int width = 2;
        int height = 2;

        Image result = converter.convert(decoded, width, height, 2);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);

        PixelReader reader = result.getPixelReader();
        assertThat(reader.getArgb(0, 0)).isEqualTo(0xFF000000);
        assertThat(reader.getArgb(1, 0)).isEqualTo(0xFFFF0000);
        assertThat(reader.getArgb(0, 1)).isEqualTo(0xFF00FF00);
        assertThat(reader.getArgb(1, 1)).isEqualTo(0xFFFFFF00);
    }

    @Test
    void testConvertStride3() {
        byte[] decoded = {0, 0, 0, -1, 0, 0, 0, -1, 0, 0, 0, -1};
        int width = 2;
        int height = 2;

        Image result = converter.convert(decoded, width, height, 3);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);

        PixelReader reader = result.getPixelReader();
        assertThat(reader.getArgb(0, 0)).isEqualTo(0xFF000000);
        assertThat(reader.getArgb(1, 0)).isEqualTo(0xFFFF0000);
        assertThat(reader.getArgb(0, 1)).isEqualTo(0xFF00FF00);
        assertThat(reader.getArgb(1, 1)).isEqualTo(0xFF0000FF);
    }

    @Test
    void testConvertStride4() {
        byte[] decoded = {
            (byte) 0xFF, 0, 0, (byte) 0xFF,
            0, (byte) 0xFF, 0, (byte) 0x7F,
            0, 0, (byte) 0xFF, (byte) 0x3F,
            0, 0, 0, 0
        };
        int width = 2;
        int height = 2;

        Image result = converter.convert(decoded, width, height, 4);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);

        PixelReader reader = result.getPixelReader();
        assertThat(reader.getArgb(0, 0)).isEqualTo(0xFFFF0000);
        assertThat(reader.getArgb(1, 0)).isEqualTo(0x7F00FF00);
        assertThat(reader.getArgb(0, 1)).isEqualTo(0x3F0000FF);
        assertThat(reader.getArgb(1, 1)).isEqualTo(0x00000000);
    }

    @Test
    void testConvertStride6() {
        byte[] decoded = new byte[6 * 4];
        ByteBuffer buffer = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer shorts = buffer.asShortBuffer();

        // Set some half-float values (these are just example values)
        shorts.put(0, (short) 0x3C00); // 1.0 in half-float
        shorts.put(1, (short) 0x0000); // 0.0 in half-float
        shorts.put(2, (short) 0x0000); // 0.0 in half-float

        shorts.put(3, (short) 0x0000); // 0.0 in half-float
        shorts.put(4, (short) 0x3C00); // 1.0 in half-float
        shorts.put(5, (short) 0x0000); // 0.0 in half-float

        shorts.put(6, (short) 0x0000); // 0.0 in half-float
        shorts.put(7, (short) 0x0000); // 0.0 in half-float
        shorts.put(8, (short) 0x3C00); // 1.0 in half-float

        shorts.put(9, (short) 0x3C00); // 1.0 in half-float
        shorts.put(10, (short) 0x3C00); // 1.0 in half-float
        shorts.put(11, (short) 0x0000); // 0.0 in half-float

        int width = 2;
        int height = 2;
        Image result = converter.convert(decoded, width, height, 6);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);

        PixelReader reader = result.getPixelReader();
        assertThat(reader.getArgb(0, 0)).isEqualTo(0xFFFF0000);
        assertThat(reader.getArgb(1, 0)).isEqualTo(0xFF00FF00);
        assertThat(reader.getArgb(0, 1)).isEqualTo(0xFF0000FF);
        assertThat(reader.getArgb(1, 1)).isEqualTo(0xFFFFFF00);
    }

    @Test
    void testUnsupportedPixelStride() {
        byte[] decoded = new byte[10];
        int width = 2;
        int height = 1;

        assertThatThrownBy(() -> converter.convert(decoded, width, height, 5))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
