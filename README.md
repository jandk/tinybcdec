# tinybcdec

[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/jandk/tinybcdec/maven.yml?logo=github)](https://github.com/jandk/tinybcdec/actions/workflows/maven.yml)
[![Maven Central Version](https://img.shields.io/maven-central/v/be.twofold/tinybcdec?logo=apachemaven)](https://central.sonatype.com/artifact/be.twofold/tinybcdec)
[![License](https://img.shields.io/github/license/jandk/tinybcdec)](https://opensource.org/licenses/MIT)

## Description

TinyBCDec is a tiny library for decoding block compressed texture formats. It's written in pure Java (11+), without any
dependencies, with a focus on speed and accuracy.

Currently, the following formats are supported:

- BC1 (DXT1)
- BC2 (DXT3)
- BC3 (DXT5)
- BC4 (ATI1)
- BC5 (ATI2)
- BC6H
- BC7

## Output format

Every format except BC6H is decoded as BGRA, 4 bytes per pixel. Read as little endian integers, that is AWT's
`TYPE_INT_ARGB` layout, so the result can go straight into a `BufferedImage` without a channel swap.

- BC1, BC2, BC3 and BC7 fill all four channels.
- BC4 is a single channel, expanded to gray: blue, green and red all get the value, alpha is 255.
- BC5 is two channels: red and green carry the data, blue is 0 and alpha is 255.
- BC6H is the exception: it is decoded as RGBA in little endian half-float (8 bytes per pixel), in that channel order
  rather than BGRA, with alpha set to 1.0.

## Usage

Using the library is straightforward, all you need is the compressed data, and the format of the data.

The following features are present:

- Partial decodes: The width and height do not need to be a multiple of the block size (4 in this case). The output
  width and height can be smaller than the input, and the library will handle this.
- BC6H: BC6H is decoded to a little endian half-float RGBA buffer, 8 bytes per pixel. The library does not provide a way
  to convert this to full float. This can be done with `Float.float16ToFloat` in Java 21, or with a library.

The library provides the `BlockDecoder` class, which can be used to decode. A new instance is created by one of the
static factory methods. You can let the library create a new buffer, or pass an existing one to save allocations.

After construction, decoders produce no heap allocations during decoding. Internal working state is pre-allocated and
reused across calls. Note that this also means instances are **not thread-safe**: each thread must use its own decoder
instance.

Both the source and the destination are a `ByteBuffer`. They are read and written by absolute index, starting at the
buffer's current position, so decoding never advances a position. Block data is little endian, so both buffers are
switched to `ByteOrder.LITTLE_ENDIAN` while decoding and set back to their original order afterwards.

Given `src` is the compressed data, positioned at the start of the blocks, the following snippet decodes a BC1 texture
into a newly allocated buffer.

```java
import be.twofold.tinybcdec.*;

BlockDecoder decoder = BlockDecoder.bc1(true);
ByteBuffer result = decoder.decode(src, 256, 256);
```

To decode into a buffer you already have, pass it along with its dimensions. There is no return value.

```java
decoder.decode(src, 256,256,dst, 256,256);
```

The destination may be smaller than the source, in which case the image is cropped to the top left. It may not be
larger, as there would be nothing to fill the remainder with.

```java
decoder.decode(src, 256,256,dst, 64,64); // the top left 64x64 pixels
```

To decode somewhere other than the top left, add the source and destination coordinates. The region decoded is whatever
is left of the destination, which makes this the form to use when the destination is already the size of the region you
want, such as a single tile.

```java
decoder.decode(src, 256,256,tile, 64,64,128,64,0,0); // the 64x64 tile at (128, 64)
```

The general form takes the region size explicitly, and is the only way to decode into part of a larger destination.

```java
decoder.decode(
    src, 256,256,     // source buffer and its dimensions
    dst, 256,256,     // destination buffer and its dimensions
        128,64,0,0,     // decode from (128, 64) to (0, 0)
        64,64             // decoding a 64x64 region
);
```

Both buffers must hold their entire image, not just the region, since the dimensions are what determine where a row
starts. `encodedByteSize(width, height)` and `decodedByteSize(width, height)` return the sizes required.

## Converting to an image

The BGRA output maps directly onto the common image types, so these snippets need no per-pixel work. They apply to every
format except BC6H, whose half-float output does not fit either.

For an AWT `BufferedImage`, the bytes are `TYPE_INT_ARGB` when read as little endian integers:

```java
ByteBuffer bgra = decoder.decode(src, width, height);
BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
bgra.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(pixels);
```

For a JavaFX `Image`, the bytes are straight-alpha BGRA, which is `PixelFormat.getByteBgraInstance()`:

```java
ByteBuffer bgra = decoder.decode(src, width, height);
WritableImage image = new WritableImage(width, height);
image.

getPixelWriter().

setPixels(
    0,0,width, height,
    PixelFormat.getByteBgraInstance(),bgra,width *4);
```

## Performance

I've done some performance testing, and the library is quite fast. I've run some benchmarks on my machine (AMD 7840U).

Some quick benchmarks, tested on a Ryzen 7840U with Oracle Java 25 (MP/s stands for megapixels per second):

- BC1: ~1050MP/s
- BC2: ~650MP/s
- BC3: ~550MP/s
- BC4: ~900MP/s
- BC5: ~450MP/s
- BC6: ~150MP/s
- BC7: ~170MP/s

These numbers are just an estimate, and can vary depending on the hardware and the JVM.

To give you an idea, this means about 16 ms to decode a 4K texture in BC1, and about 100 ms for BC6 or BC7.

## Accuracy

A final note on accuracy, the library is tested against the output of DirectXTex. I generated images, encoded them,
decoded them again and compared the output. The output is identical, except for signed BC4 and BC5, which are
different by a small amount. This is due to the way they are handled in DirectXTex, which is different from the
unsigned code path. As far as I can test, the values are correct on my side. A bug has been filed.

This is done by using fixed point arithmetic for BC1 through 5. Check out
[GenerateRescale.java](https://github.com/jandk/tinybcdec/blob/main/src/test/java/be/twofold/tinybcdec/GenerateRescale.java)
for how these scale factors are calculated. My unsigned results all line up with what
[bcdec](https://github.com/iOrange/bcdec/) does.
