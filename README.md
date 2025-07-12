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

For ease of use, the library provides functionality to reorder the output channels, so the decoded data can be used
directly AWT, JavaFX, or even OpenGL or Vulkan.

## Usage

Using the library is straightforward, all you need is the compressed data, and the format of the data.

The following features are present:

- Pixel order with alpha filling: If the pixel order contains an alpha channel, and the source does not, the alpha
  channel will be set to `255` or `1.0` in the case of BC6H.
- Partial decodes: The width and height do not need to be a multiple of the block size (4 in this case).
- Z reconstruction ("Blue" normal maps): BC5 uses two channels to store a normal map, the library can reconstruct the Z
  value from the two channels. Clamping is done to prevent negative values or values above 255.
- BC6H: BC6H is decoded to a Little-Endian Half-Float buffer. The library does not provide a way to convert this to
  full float. This can be done with `Float.float16toFloat` in Java 21, or with a library. This also means that the
  output buffer is twice as big.

The library provides the `BlockDecoder` class, which can be used to decode, by specifying the format and the pixel
order. You can let the library create a new buffer, or pass an existing one to save allocations.

Given `src` is the compressed data, starting at offset `srcPos`, the following code snippet shows how to decode a BC1
texture.

```java
import be.twofold.tinybcdec.*;

BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC1);
byte[] result = decoder.decode(256, 256, src, srcPos);
```

If you want to pass an existing buffer, you can pass it as the last two arguments `dst` and `dstPos`. There will be no
return value.

```java
decoder.decode(256, 256, src, srcPos, dst, dstPos);
```

## Performance

I've done some performance testing, and the library is quite fast. I've run some benchmarks on my machine (AMD 7840U).

Some quick benchmarks, tested on a Ryzen 5950X:

- BC1: ~1050MP/s
- BC2: ~650MP/s
- BC3: ~500MP/s
- BC4: ~550MP/s
- BC5: ~425MP/s
- BC6: ~130MP/s
- BC7: ~150MP/s

These numbers are just an estimate, and can vary depending on the hardware and the JVM.

To give you an idea, this means about 30 ms to decode a 4K texture in BC1, and about 120 ms for BC6 or BC7.

## Accuracy

A final note on accuracy, the library is tested against the output of DirectXTex. I generated images, encoded them,
decoded them again and compared the output. The output is identical, except when reconstructing Z in BC5, where the
library uses a different method.

This is done by doing a full float implementation of BC1 (and by extension BC2 and BC3). The other formats have
bit-exact implementations. Z reconstruction uses a lookup table that is generated at runtime, and is also full float
accurate.
