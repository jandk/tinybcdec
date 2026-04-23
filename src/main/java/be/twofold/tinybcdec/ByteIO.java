package be.twofold.tinybcdec;

import java.lang.invoke.*;
import java.nio.*;

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

    private ByteIO() {
    }

    static byte getByte(ByteBuffer buffer, int index) {
        if (buffer.hasArray()) {
            return buffer.array()[buffer.arrayOffset() + index];
        } else {
            return buffer.get(index);
        }
    }

    static void setByte(ByteBuffer buffer, int index, byte value) {
        if (buffer.hasArray()) {
            buffer.array()[buffer.arrayOffset() + index] = value;
        } else {
            buffer.put(index, value);
        }
    }

    static void setShort(ByteBuffer buffer, int index, short value) {
        if (buffer.hasArray()) {
            VH_SHORT.set(buffer.array(), buffer.arrayOffset() + index, value);
        } else {
            buffer.putShort(index, value);
        }
    }

    static void setInt(ByteBuffer buffer, int index, int value) {
        if (buffer.hasArray()) {
            VH_INT.set(buffer.array(), buffer.arrayOffset() + index, value);
        } else {
            buffer.putInt(index, value);
        }
    }

    static long getLong(ByteBuffer buffer, int index) {
        if (buffer.hasArray()) {
            return (long) VH_LONG.get(buffer.array(), buffer.arrayOffset() + index);
        } else {
            return buffer.getLong(index);
        }
    }
}
