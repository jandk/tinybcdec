package be.twofold.tinybcdec;

/**
 * Represents the order of the color channels in a pixel.
 * <p>
 * {@code -1} is used to indicate that a channel is not present.
 * <p>
 * Some of these have handy usages, hence why they're constants.
 * <ul>
 *     <li>{@link #RGBA} and {@link #RGB} - Useful for exporting as raw arrays, or to feed into a png encoder</li>
 *     <li>{@link #BGRA} - Useful for JavaFX PixelBuffer</li>
 *     <li>{@link #ABGR} and {@link #BGR} - Useful for AWT BufferedImages</li>
 * </ul>
 */
public final class PixelOrder {

    /**
     * A standard order for RGBA colors.
     * <p>
     * The red channel is at index 0, the green channel is at index 1, the blue channel is at index 2, and the alpha channel is at index 3.
     */
    public static final PixelOrder RGBA = of(4, 0, 1, 2, 3);

    /**
     * A standard order for BGRA colors.
     * <p>
     * The red channel is at index 2, the green channel is at index 1, the blue channel is at index 0, and the alpha channel is at index 3.
     */
    public static final PixelOrder BGRA = of(4, 2, 1, 0, 3);

    /**
     * A standard order for ARGB colors.
     * <p>
     * The red channel is at index 3, the green channel is at index 2, the blue channel is at index 1, and the alpha channel is at index 0.
     */
    public static final PixelOrder ARGB = of(4, 1, 2, 3, 0);

    /**
     * A standard order for ABGR colors.
     * <p>
     * The red channel is at index 3, the green channel is at index 2, the blue channel is at index 1, and the alpha channel is at index 0.
     */
    public static final PixelOrder ABGR = of(4, 3, 2, 1, 0);

    /**
     * A standard order for RGB colors.
     *
     * <p>The red channel is at index 0, the green channel is at index 1, and the blue channel is at index 2.
     */
    public static final PixelOrder RGB = of(3, 0, 1, 2, -1);

    /**
     * A standard order for BGR colors.
     *
     * <p>The blue channel is at index 0, the green channel is at index 1, and the red channel is at index 2.
     */
    public static final PixelOrder BGR = of(3, 2, 1, 0, -1);

    /**
     * A standard order for RG colors.
     *
     * <p>The red channel is at index 0, the green channel is at index 1. This is often used for normal maps.</p>
     */
    public static final PixelOrder RG = of(2, 0, 1, -1, -1);

    /**
     * A standard order for grayscale colors.
     *
     * <p>The red channel is at index 0.
     */
    public static final PixelOrder R = of(1, 0, -1, -1, -1);

    private final int count;
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    private PixelOrder(int count, int red, int green, int blue, int alpha) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1");
        }
        this.count = count;
        this.red = validate(count, red, "red");
        this.green = validate(count, green, "green");
        this.blue = validate(count, blue, "blue");
        this.alpha = validate(count, alpha, "alpha");

        if (overlap(red, green) || overlap(red, blue) || overlap(red, alpha) || overlap(green, blue) || overlap(green, alpha) || overlap(blue, alpha)) {
            throw new IllegalArgumentException("channels must not overlap");
        }
    }

    private static int validate(int count, int channel, String color) {
        if (channel != -1 && (channel < 0 || channel >= count)) {
            throw new IllegalArgumentException(color + " must be in the range [0, " + count + ")");
        }
        return channel;
    }

    private static boolean overlap(int a, int b) {
        return a != -1 && a == b;
    }

    /**
     * Creates a new order with the specified channels.
     *
     * @param count the number of channels
     * @param red   the index of the red channel
     * @param green the index of the green channel
     * @param blue  the index of the blue channel
     * @param alpha the index of the alpha channel
     * @return the new order instance
     * @throws IllegalArgumentException if the count is less than 1
     *                                  or if any of the channels are out of bounds,
     *                                  or if any of the channels overlap
     */
    public static PixelOrder of(int count, int red, int green, int blue, int alpha) {
        return new PixelOrder(count, red, green, blue, alpha);
    }

    /**
     * Creates a new order with the specified channels.
     *
     * @param count   the number of channels
     * @param channel the index of the channel
     * @return the new order instance
     * @throws IllegalArgumentException if the count is less than 1 or if channel is out of bounds
     */
    public static PixelOrder single(int count, int channel) {
        return of(count, channel, -1, -1, -1);
    }

    /**
     * Returns the number of channels.
     *
     * @return the number of channels
     */
    public int count() {
        return count;
    }

    /**
     * Returns the index of the red channel.
     *
     * @return the index of the red channel
     */
    public int red() {
        return red;
    }

    /**
     * Returns the index of the green channel.
     *
     * @return the index of the green channel
     */
    public int green() {
        return green;
    }

    /**
     * Returns the index of the blue channel.
     *
     * @return the index of the blue channel
     */
    public int blue() {
        return blue;
    }

    /**
     * Returns the index of the alpha channel.
     *
     * @return the index of the alpha channel
     */
    public int alpha() {
        return alpha;
    }

    @Override
    public String toString() {
        return "Channels(" +
            "count=" + count + ", " +
            "red=" + red + ", " +
            "green=" + green + ", " +
            "blue=" + blue + ", " +
            "alpha=" + alpha +
            ")";
    }
}
