package be.twofold.tinybcdec;

/**
 * Determines the alpha channel handling for BCn texture decompression.
 */
public enum Opacity {
    /**
     * Block contains only opaque pixels (alpha = 255 or 1.0).
     */
    OPAQUE,
    /**
     * Block contains pixels with varying alpha values.
     */
    TRANSPARENT,
}
