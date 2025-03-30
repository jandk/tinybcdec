package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class PlatformTest {
    @Test
    void testFloat16ToFloat() {
        // Sanity check to see if the method resolving works
        assertThat(Platform.float16ToFloat((short) 0)).isEqualTo(0.0f);
    }

    @Test
    void testFloat16ToFloat0() throws IOException {
        byte[] bytes = BCTestUtils.readResource("/float16ToFloat.bin");
        var buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();

        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            float actual = Platform.float16ToFloat0((short) i);
            float expected = buffer.get();
            if (Float.isNaN(expected)) {
                assertThat(actual).isNaN();
            } else {
                assertThat(actual).isEqualTo(expected);
            }
        }
    }

    // // To generate this, run this method on a JVM >= 20
    // public static void main(String[] args) throws IOException {
    //     var buffer = ByteBuffer
    //         .allocate((Short.MAX_VALUE - Short.MIN_VALUE + 1) * Float.BYTES)
    //         .order(ByteOrder.LITTLE_ENDIAN);
    //
    //     var floatBuffer = buffer.asFloatBuffer();
    //     for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
    //         floatBuffer.put(Float.float16ToFloat((short) i));
    //     }
    //
    //     Files.write(Path.of("float16ToFloat.bin"), buffer.array());
    // }
}
