# tinybcdec

![Build Status](https://github.com/jandk/tinybcdec/actions/workflows/maven.yml/badge.svg)

![License](https://img.shields.io/github/license/jandk/tinybcdec)

## Description

TinyBCDec is a tiny library for decoding block compressed texture formats. It's written in pure Java, without any
dependencies, with a focus on speed and accuracy.

Currently, the following formats are supported:

- BC1 (DXT1)
- BC2 (DXT3)
- BC3 (DXT5)
- BC4 (ATI1)
- BC5 (ATI2)
- BC6H (BPTC)
- BC7

For ease of use, the library provides functionality to reorder the output channels, so the decoded data can be used
directly AWT, JavaFX, or even OpenGL or Vulkan.

## Usage

Using the library is very easy, all you need is the compressed data, and the format of the data.

The library provides a `BlockDecoder` class, which can be used to decode, by specifying the format and the pixel order.

Optionally you can provide your own output buffer, or let the library allocate one for you.

Given `data` is the compressed data, the following code snippet shows how to decode a BC1 texture:

```java
import be.twofold.tinybcdec.*;

BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC1, PixelOrder.RGBA);
byte[] result = decoder.decode(256, 256, data, 0);
```

## Performance

I've done some performance testing, and the library is quite fast. I've run some benchmarks on my machine (AMD 7840U).

Some quick benchmarks:

- BC1: ~600MP/s
- BC2: ~450MP/s
- BC3: ~400MP/s
- BC4: ~900MP/s
- BC5: ~450MP/s
- BC6: ~100MP/s
- BC7: ~100MP/s

These numbers are just a rough estimate, and can vary depending on the hardware and the JVM.

For an idea, this means about 30ms to decode a 4K texture in BC1, and about 120ms for BC6 or BC7.

## Accuracy

A final note on accuracy, the library is tested against the output of DirectXTex. I generated images, encoded them,
decoded them again and compared the output. The output is identical, except when reconstructing Z in BC5, where the
library uses a different method.

This is done by doing a full float implementation of BC1 (and by extension BC2 and BC3). The other formats have
bit-exact implementations.
