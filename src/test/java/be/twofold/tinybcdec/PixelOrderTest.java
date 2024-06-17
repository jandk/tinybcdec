package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class PixelOrderTest {

    @Test
    void testRGBA() {
        PixelOrder order = PixelOrder.RGBA;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(2);
        assertThat(order.alpha()).isEqualTo(3);
    }

    @Test
    void testBGRA() {
        PixelOrder order = PixelOrder.BGRA;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(2);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(0);
        assertThat(order.alpha()).isEqualTo(3);
    }

    @Test
    void testARGB() {
        PixelOrder order = PixelOrder.ARGB;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(1);
        assertThat(order.green()).isEqualTo(2);
        assertThat(order.blue()).isEqualTo(3);
        assertThat(order.alpha()).isEqualTo(0);
    }

    @Test
    void testABGR() {
        PixelOrder order = PixelOrder.ABGR;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(3);
        assertThat(order.green()).isEqualTo(2);
        assertThat(order.blue()).isEqualTo(1);
        assertThat(order.alpha()).isEqualTo(0);
    }

    @Test
    void testRGB() {
        PixelOrder order = PixelOrder.RGB;
        assertThat(order.count()).isEqualTo(3);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(2);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testBGR() {
        PixelOrder order = PixelOrder.BGR;
        assertThat(order.count()).isEqualTo(3);
        assertThat(order.red()).isEqualTo(2);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(0);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testR() {
        PixelOrder order = PixelOrder.R;
        assertThat(order.count()).isEqualTo(1);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(-1);
        assertThat(order.blue()).isEqualTo(-1);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testOf() {
        PixelOrder order = PixelOrder.of(4, 0, 1, 2, 3);
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(2);
        assertThat(order.alpha()).isEqualTo(3);
    }

    @Test
    void testSingle() {
        PixelOrder order = PixelOrder.single(4, 0);
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(-1);
        assertThat(order.blue()).isEqualTo(-1);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testValidation() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(0, 0, 1, 2, 3))
            .withMessage("count must be at least 1");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 4, -1, -1, -1))
            .withMessage("red must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -1, 4, -1, -1))
            .withMessage("green must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -1, -1, 4, -1))
            .withMessage("blue must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -1, -1, -1, 4))
            .withMessage("alpha must be in the range [0, 4)");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -2, -1, -1, -1))
            .withMessage("red must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -1, -2, -1, -1))
            .withMessage("green must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -1, -1, -2, -1))
            .withMessage("blue must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, -1, -1, -1, -2))
            .withMessage("alpha must be in the range [0, 4)");
    }

    @Test
    void testOverlapNotAllowed() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 0, 0, 2, 3))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 0, 1, 0, 3))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 0, 1, 2, 0))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 0, 1, 1, 3))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 0, 1, 2, 1))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PixelOrder.of(4, 0, 1, 2, 2))
            .withMessage("channels must not overlap");
    }

}
