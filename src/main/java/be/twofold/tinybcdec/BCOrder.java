package be.twofold.tinybcdec;

public final class BCOrder {
    public static BCOrder RGBA = of(4, 0, 1, 2, 3);
    public static BCOrder BGRA = of(4, 2, 1, 0, 3);
    public static BCOrder ABGR = of(4, 3, 2, 1, 0);
    public static BCOrder RGB = of(3, 0, 1, 2, -1);
    public static BCOrder BGR = of(3, 2, 1, 0, -1);
    public static BCOrder R = of(1, 0, -1, -1, -1);

    private final int count;
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    private BCOrder(int count, int red, int green, int blue, int alpha) {
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

    public static BCOrder of(int count, int red, int green, int blue, int alpha) {
        return new BCOrder(count, red, green, blue, alpha);
    }

    public static BCOrder single(int count, int channel) {
        return of(count, channel, -1, -1, -1);
    }

    public int count() {
        return count;
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }

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
