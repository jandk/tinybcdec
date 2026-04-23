package be.twofold.tinybcdec;

import java.lang.invoke.*;
import java.nio.*;

final class ByteArrays {
    private static final VarHandle ShortVarHandle =
        MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle IntVarHandle =
        MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle LongVarHandle =
        MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle FloatVarHandle =
        MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN);

    private ByteArrays() {
    }

    static void setByte(ByteBuffer buffer, int index, byte value) {
        buffer.put(index, value);
    }

    static short getShort(ByteBuffer buffer, int index) {
        return buffer.getShort(index);
    }

    static void setShort(ByteBuffer buffer, int index, short value) {
        buffer.putShort(index, value);
    }

    static int getInt(ByteBuffer buffer, int index) {
        return buffer.getInt(index);
    }

    static void setInt(ByteBuffer buffer, int index, int value) {
        buffer.putInt(index, value);
    }

    static long getLong(ByteBuffer buffer, int index) {
        return buffer.getLong(index);
    }

    static void setFloat(ByteBuffer buffer, int index, float value) {
        buffer.putFloat(index, value);
    }
}
