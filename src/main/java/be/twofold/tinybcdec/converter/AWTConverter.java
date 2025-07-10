package be.twofold.tinybcdec.converter;

import be.twofold.tinybcdec.*;
import be.twofold.tinybcdec.utils.*;

import java.awt.color.*;
import java.awt.image.*;
import java.nio.*;

public final class AWTConverter extends Converter<BufferedImage> {
    @Override
    protected BufferedImage convert(int width, int height, byte[] decoded, int pixelStride) {
        if (pixelStride == 1) {
            return convertStride1(width, height, decoded);
        } else if (pixelStride == 3) {
            return convertStride3(width, height, decoded);
        } else if (pixelStride == 4) {
            return convertStride4(width, height, decoded);
        } else if (pixelStride >= 6) {
            return convertStride6Plus(width, height, decoded, pixelStride);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static BufferedImage convertStride1(int width, int height, byte[] decoded) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setDataElements(0, 0, width, height, decoded);
        return image;
    }


    private BufferedImage convertStride3(int width, int height, byte[] decoded) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0, o = 0, len = decoded.length; i < len; i += 3, o++) {
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

        for (int i = 0, o = 0, len = decoded.length; i < len; i += 4, o++) {
            rawImage[o] = swapRB(ByteArrays.getInt(decoded, i));
        }
        return image;
    }

    private static BufferedImage convertStride6Plus(int width, int height, byte[] decoded, int pixelStride) {
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

        ByteBuffer buffer = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN);
        float[] rawImage = ((DataBufferFloat) image.getRaster().getDataBuffer()).getData();

        if (pixelStride == 6) {
            ShortBuffer shorts = buffer.asShortBuffer();
            for (int i = 0, len = shorts.capacity(); i < len; i++) {
                rawImage[i] = Platform.float16ToFloat(shorts.get(i));
            }
        } else if (pixelStride == 12) {
            buffer.asFloatBuffer().get(rawImage);
        } else {
            throw new UnsupportedOperationException();
        }
        return image;
    }
}
