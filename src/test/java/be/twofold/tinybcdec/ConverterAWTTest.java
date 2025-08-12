package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.awt.image.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class ConverterAWTTest {
    private Converter<BufferedImage> converter;

    @BeforeEach
    void setUp() {
        converter = Converter.awt(false);
    }

    @Test
    void testConvertStride1() {
        byte[] decoded = {0, 50, 100, (byte) 150, (byte) 200, (byte) 255};
        int width = 3;
        int height = 2;

        BufferedImage result = converter.convert(decoded, width, height, 1);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);
        assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_BYTE_GRAY);

        byte[] resultData = ((DataBufferByte) result.getRaster().getDataBuffer()).getData();
        assertThat(resultData).isEqualTo(decoded);
    }

    @Test
    void testConvertStride2() {
        byte[] decoded = {0, 0, -1, 0, 0, -1, -1, -1};
        int width = 2;
        int height = 2;

        BufferedImage result = converter.convert(decoded, width, height, 2);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);
        assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);

        int[] resultData = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        assertThat(resultData[0]).isEqualTo(0xFF000000);
        assertThat(resultData[1]).isEqualTo(0xFFFF0000);
        assertThat(resultData[2]).isEqualTo(0xFF00FF00);
        assertThat(resultData[3]).isEqualTo(0xFFFFFF00);
    }

    @Test
    void testConvertStride3() {
        byte[] decoded = {
            (byte) 255, 0, 0,
            0, (byte) 255, 0,
            0, 0, (byte) 255,
            (byte) 255, (byte) 255, 0
        };
        int width = 2;
        int height = 2;

        BufferedImage result = converter.convert(decoded, width, height, 3);


        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);
        assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_RGB);


        int[] resultData = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        assertThat(resultData[0]).isEqualTo(0xFFFF0000);
        assertThat(resultData[1]).isEqualTo(0xFF00FF00);
        assertThat(resultData[2]).isEqualTo(0xFF0000FF);
        assertThat(resultData[3]).isEqualTo(0xFFFFFF00);
    }

    @Test
    void testConvertStride4() {
        byte[] decoded = {
            0, 0, (byte) 255, (byte) 255,
            0, (byte) 255, 0, (byte) 128,
            (byte) 255, 0, 0, (byte) 64,
            (byte) 255, (byte) 255, 0, 0
        };
        int width = 2;
        int height = 2;

        BufferedImage result = converter.convert(decoded, width, height, 4);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);
        assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);

        int[] resultData = ((DataBufferInt) result.getRaster().getDataBuffer()).getData();
        assertThat(resultData[0]).isEqualTo(0xFF0000FF);
        assertThat(resultData[1]).isEqualTo(0x8000FF00);
        assertThat(resultData[2]).isEqualTo(0x40FF0000);
        assertThat(resultData[3]).isEqualTo(0x00FFFF00);
    }

    @Test
    void testConvertStride6() {
        byte[] decoded = new byte[6 * 4];
        ByteBuffer buffer = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer shorts = buffer.asShortBuffer();

        shorts.put(0, (short) 0x3C00);
        shorts.put(1, (short) 0x0000);
        shorts.put(2, (short) 0x0000);

        shorts.put(3, (short) 0x0000);
        shorts.put(4, (short) 0x3C00);
        shorts.put(5, (short) 0x0000);

        shorts.put(6, (short) 0x0000);
        shorts.put(7, (short) 0x0000);
        shorts.put(8, (short) 0x3C00);

        shorts.put(9, (short) 0x3C00);
        shorts.put(10, (short) 0x3C00);
        shorts.put(11, (short) 0x0000);

        int width = 2;
        int height = 2;

        BufferedImage result = converter.convert(decoded, width, height, 6);

        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(width);
        assertThat(result.getHeight()).isEqualTo(height);
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
