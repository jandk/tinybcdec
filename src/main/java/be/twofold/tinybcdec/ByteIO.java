package be.twofold.tinybcdec;

import java.lang.invoke.*;
import java.nio.*;
import java.util.*;

/**
 * ByteBuffer has many concrete subclasses (heap, direct, read-only, sliced, ...). Call sites that
 * see 3+ receiver types go megamorphic and the JIT stops inlining, causing severe throughput drops.
 * Routing through hasArray() keeps the hot path on a plain byte[] + VarHandle, which the JIT
 * compiles to a single load/store instruction regardless of how many ByteBuffer types are in play.
 * <p>
 * I can't believe I had to do this, but it drops the perf hit from 30% all the way down to 4% for BC1.
 * Slower codecs lose less.
 */
final class ByteIO {
    private static final VarHandle VH_SHORT =
        MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle VH_INT =
        MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle VH_LONG =
        MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle VH_FLOAT =
        MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN);

    private ByteIO() {
    }

    static byte getByte(ByteBuffer buffer, int offset) {
        if (buffer.hasArray()) {
            return buffer.array()[buffer.arrayOffset() + offset];
        } else {
            return buffer.get(offset);
        }
    }

    static void setByte(ByteBuffer buffer, int offset, byte value) {
        if (buffer.hasArray()) {
            buffer.array()[buffer.arrayOffset() + offset] = value;
        } else {
            buffer.put(offset, value);
        }
    }

    static void setShort(ByteBuffer buffer, int offset, short value) {
        if (buffer.hasArray()) {
            VH_SHORT.set(buffer.array(), buffer.arrayOffset() + offset, value);
        } else {
            buffer.putShort(offset, value);
        }
    }

    static void setInt(ByteBuffer buffer, int offset, int value) {
        if (buffer.hasArray()) {
            VH_INT.set(buffer.array(), buffer.arrayOffset() + offset, value);
        } else {
            buffer.putInt(offset, value);
        }
    }

    static void setLong(ByteBuffer buffer, int offset, long value) {
        if (buffer.hasArray()) {
            VH_LONG.set(buffer.array(), buffer.arrayOffset() + offset, value);
        } else {
            buffer.putLong(offset, value);
        }
    }

    static long getLong(ByteBuffer buffer, int offset) {
        if (buffer.hasArray()) {
            return (long) VH_LONG.get(buffer.array(), buffer.arrayOffset() + offset);
        } else {
            return buffer.getLong(offset);
        }
    }

    public static void setFloat(ByteBuffer buffer, int offset, float value) {
        if (buffer.hasArray()) {
            VH_FLOAT.set(buffer.array(), buffer.arrayOffset() + offset, value);
        } else {
            buffer.putFloat(offset, value);
        }
    }

    static void copy(ByteBuffer src, int srcOff, ByteBuffer dst, int dstOff, int length) {
        if (src.hasArray() && dst.hasArray()) {
            srcOff += src.arrayOffset();
            dstOff += dst.arrayOffset();
            System.arraycopy(src.array(), srcOff, dst.array(), dstOff, length);
        } else {
            for (int i = 0; i < length; i++) {
                dst.put(dstOff + i, src.get(srcOff + i));
            }
        }
    }

    static void fill(ByteBuffer buffer, int offset, int length, byte value) {
        if (buffer.hasArray()) {
            offset += buffer.arrayOffset();
            Arrays.fill(buffer.array(), offset, offset + length, value);
        } else {
            for (int i = 0; i < length; i++) {
                buffer.put(offset + i, value);
            }
        }
    }
}
