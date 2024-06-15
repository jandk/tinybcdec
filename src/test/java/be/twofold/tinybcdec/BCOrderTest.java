package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class BCOrderTest {

    @Test
    void testRGBA() {
        BCOrder order = BCOrder.RGBA;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(2);
        assertThat(order.alpha()).isEqualTo(3);
    }

    @Test
    void testBGRA() {
        BCOrder order = BCOrder.BGRA;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(2);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(0);
        assertThat(order.alpha()).isEqualTo(3);
    }

    @Test
    void testABGR() {
        BCOrder order = BCOrder.ABGR;
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(3);
        assertThat(order.green()).isEqualTo(2);
        assertThat(order.blue()).isEqualTo(1);
        assertThat(order.alpha()).isEqualTo(0);
    }

    @Test
    void testRGB() {
        BCOrder order = BCOrder.RGB;
        assertThat(order.count()).isEqualTo(3);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(2);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testBGR() {
        BCOrder order = BCOrder.BGR;
        assertThat(order.count()).isEqualTo(3);
        assertThat(order.red()).isEqualTo(2);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(0);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testR() {
        BCOrder order = BCOrder.R;
        assertThat(order.count()).isEqualTo(1);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(-1);
        assertThat(order.blue()).isEqualTo(-1);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testOf() {
        BCOrder order = BCOrder.of(4, 0, 1, 2, 3);
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(1);
        assertThat(order.blue()).isEqualTo(2);
        assertThat(order.alpha()).isEqualTo(3);
    }

    @Test
    void testSingle() {
        BCOrder order = BCOrder.single(4, 0);
        assertThat(order.count()).isEqualTo(4);
        assertThat(order.red()).isEqualTo(0);
        assertThat(order.green()).isEqualTo(-1);
        assertThat(order.blue()).isEqualTo(-1);
        assertThat(order.alpha()).isEqualTo(-1);
    }

    @Test
    void testValidation() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(0, 0, 1, 2, 3))
            .withMessage("count must be at least 1");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 4, -1, -1, -1))
            .withMessage("red must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -1, 4, -1, -1))
            .withMessage("green must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -1, -1, 4, -1))
            .withMessage("blue must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -1, -1, -1, 4))
            .withMessage("alpha must be in the range [0, 4)");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -2, -1, -1, -1))
            .withMessage("red must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -1, -2, -1, -1))
            .withMessage("green must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -1, -1, -2, -1))
            .withMessage("blue must be in the range [0, 4)");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, -1, -1, -1, -2))
            .withMessage("alpha must be in the range [0, 4)");
    }

    @Test
    void testOverlapNotAllowed() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 0, 0, 2, 3))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 0, 1, 0, 3))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 0, 1, 2, 0))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 0, 1, 1, 3))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 0, 1, 2, 1))
            .withMessage("channels must not overlap");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BCOrder.of(4, 0, 1, 2, 2))
            .withMessage("channels must not overlap");
    }

}
