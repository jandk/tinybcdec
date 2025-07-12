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

    static void setShort(byte[] array, int index, short value) {
        ShortVarHandle.set(array, index, value);
    }

    static void setInt(byte[] array, int index, int value) {
        IntVarHandle.set(array, index, value);
    }

    static void setFloat(byte[] array, int index, float value) {
        FloatVarHandle.set(array, index, value);
    }

    static int getInt(byte[] array, int index) {
        return (int) IntVarHandle.get(array, index);
    }

    static long getLong(byte[] array, int index) {
        return (long) LongVarHandle.get(array, index);
    }
}
