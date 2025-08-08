package be.twofold.tinybcdec;

import java.awt.color.*;
import java.awt.image.*;

final class ConverterAWT extends Converter<BufferedImage> {
    @Override
    BufferedImage convert(int width, int height, byte[] decoded, int pixelStride) {
        switch (pixelStride) {
            case 1:
                return convertStride1(width, height, decoded);
            case 3:
                return convertStride3(width, height, decoded);
            case 4:
                return convertStride4(width, height, decoded);
            case 6:
                return convertStride6(width, height, decoded);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private BufferedImage convertStride1(int width, int height, byte[] decoded) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setDataElements(0, 0, width, height, decoded);
        return image;
    }

    private BufferedImage convertStride3(int width, int height, byte[] decoded) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0; i < decoded.length; i += 3, o++) {
            int r = Byte.toUnsignedInt(decoded[i/**/]);
            int g = Byte.toUnsignedInt(decoded[i + 1]);
            int b = Byte.toUnsignedInt(decoded[i + 2]);
            rawImage[o] = 0xFF000000 | r << 16 | g << 8 | b;
        }
        return image;
    }

    private BufferedImage convertStride4(int width, int height, byte[] decoded) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0; i < decoded.length; i += 4, o++) {
            rawImage[o] = swapRB(ByteArrays.getInt(decoded, i));
        }
        return image;
    }

    private BufferedImage convertStride6(int width, int height, byte[] decoded) {
        ColorModel colorModel = new ComponentColorModel(
            ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
            false,
            false,
            ColorModel.OPAQUE,
            DataBuffer.TYPE_FLOAT
        );
        BufferedImage image = new BufferedImage(
            colorModel,
            colorModel.createCompatibleWritableRaster(width, height),
            colorModel.isAlphaPremultiplied(),
            null
        );

        float[] rawImage = ((DataBufferFloat) image.getRaster().getDataBuffer()).getData();
        for (int i = 0, o = 0; i < decoded.length; i += 2, o++) {
            rawImage[o] = Platform.float16ToFloat(ByteArrays.getShort(decoded, i));
        }
        return image;
    }
}
