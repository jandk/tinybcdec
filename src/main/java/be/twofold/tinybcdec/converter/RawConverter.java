package be.twofold.tinybcdec.converter;

import be.twofold.tinybcdec.*;

public final class RawConverter extends Converter<byte[]> {
    @Override
    protected byte[] convert(int width, int height, byte[] decoded, int pixelStride) {
        return decoded;
    }
}
