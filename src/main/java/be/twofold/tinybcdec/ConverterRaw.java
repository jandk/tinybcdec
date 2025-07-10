package be.twofold.tinybcdec;

final class ConverterRaw extends Converter<byte[]> {
    @Override
    byte[] convert(int width, int height, byte[] decoded, int pixelStride) {
        return decoded;
    }
}
