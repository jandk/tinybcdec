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

- BC1, BC2, BC3 and BC7 are decoded as RGBA (4 bytes per pixel).
- BC4 is decoded as R (1 byte per pixel).
- BC5 is decoded as RG (2 bytes per pixel).
- BC6H is decoded as RGB in little endian half-float (6 bytes per pixel).

## Usage

Using the library is straightforward, all you need is the compressed data, and the format of the data.

The following features are present:

- Partial decodes: The width and height do not need to be a multiple of the block size (4 in this case). The output
  width and height can be smaller than the input, and the library will handle this.
- BC6H: BC6H is decoded to a little endian half-float buffer. The library does not provide a way to convert this to
  full float. This can be done with `Float.float16toFloat` in Java 21, or with a library. This also means that the
  output buffer is twice as big.

The library provides the `BlockDecoder` class, which can be used to decode. A new instance is created by one of the
static factory methods. You can let the library create a new buffer, or pass an existing one to save allocations.

Given `src` is the compressed data, starting at offset `srcPos`, the following code snippet shows how to decode a BC1
texture.

```java
import be.twofold.tinybcdec.*;

BlockDecoder decoder = BlockDecoder.bc1(BlockDecoder.Opacity.OPAQUE);
byte[] result = decoder.decode(src, srcPos, 256, 256);
```

If you want to pass an existing buffer, you can pass it as the last two arguments `dst` and `dstPos`. There will be no
return value.

```java
decoder.decode(src, srcPos, 256, 256, dst, dstPos);
```

If you want to decode a partial image,

## Performance

I've done some performance testing, and the library is quite fast. I've run some benchmarks on my machine (AMD 7840U).

Some quick benchmarks, tested on a Ryzen 7840U with Oracle Java 21 (MP/s stands for megapixels per second):

- BC1: ~1050MP/s
- BC2: ~750MP/s
- BC3: ~550MP/s
- BC4: ~1000MP/s
- BC5: ~550MP/s
- BC6: ~155MP/s
- BC7: ~175MP/s

These numbers are just an estimate, and can vary depending on the hardware and the JVM.

To give you an idea, this means about 20 ms to decode a 4K texture in BC1, and about 100 ms for BC6 or BC7.

## Accuracy

A final note on accuracy, the library is tested against the output of DirectXTex. I generated images, encoded them,
decoded them again and compared the output. The output is identical, except for signed BC4 and BC5, which are
different by a small amount. This is due to the way they are handled in DirectXTex, which is different from the
unsigned code path. As far as I can test, the values are correct on my side. A bug has been filed.

This is done by using fixed point arithmetic for BC1 through 5. Check out
[GenerateRescale.java](https://github.com/jandk/tinybcdec/blob/main/src/test/java/be/twofold/tinybcdec/GenerateRescale.java)
for how these scale factors are calculated. My unsigned results all line up with what
[bcdec](https://github.com/iOrange/bcdec/) does.
