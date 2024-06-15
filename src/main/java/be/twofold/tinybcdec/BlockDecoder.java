package be.twofold.tinybcdec;

import java.util.*;

public final class BlockDecoder {
    private final BCFormat format;
    private final BCDecoder decoder;
    private final int bytesPerPixel;
    private final int rOffset;
    private final int gOffset;
    private final int bOffset;
    private final int aOffset;
    final byte[] buffer = new byte[128];

    public BlockDecoder(BCFormat format, int bytesPerPixel, int rOffset, int gOffset, int bOffset, int aOffset) {
        Objects.requireNonNull(format, "format must not be null");
        if (bytesPerPixel < format.minBytesPerPixel()) {
            throw new IllegalArgumentException("bytesPerPixel must be at least " + format.minBytesPerPixel());
        }
        if (rOffset != -1 && rOffset < 0 || rOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("rOffset must be in the range [0, bytesPerPixel)");
        }
        if (gOffset != -1 && gOffset < 0 || gOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("gOffset must be in the range [0, bytesPerPixel)");
        }
        if (bOffset != -1 && bOffset < 0 || bOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("bOffset must be in the range [0, bytesPerPixel)");
        }
        if (aOffset != -1 && aOffset < 0 || aOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("aOffset must be in the range [0, bytesPerPixel)");
        }

        this.format = format;
        this.decoder = createDecoder(format);
        this.bytesPerPixel = bytesPerPixel;
        this.rOffset = rOffset;
        this.gOffset = gOffset;
        this.bOffset = bOffset;
        this.aOffset = aOffset;
    }

    private BCDecoder createDecoder(BCFormat format) {
        switch (format) {
            case BC1:
                return new BC1Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, false);
            case BC2:
                return new BC2Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
            case BC3:
                return new BC3Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
            case BC4U:
                return new BC4UDecoder(bytesPerPixel, rOffset);
            case BC5U:
                return new BC5UDecoder(bytesPerPixel, rOffset, gOffset);
            case BC5U_BLUE:
                return new BC5UDecoder(bytesPerPixel, rOffset, gOffset, bOffset);
            case BC6H_UF16:
                return new BC6HDecoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, false);
            case BC6H_SF16:
                return new BC6HDecoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, true);
            case BC7:
                return new BC7Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
}
