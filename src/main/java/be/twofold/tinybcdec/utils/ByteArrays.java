package be.twofold.tinybcdec.utils;

import java.lang.invoke.*;
import java.nio.*;

public final class ByteArrays {
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

    public static void setShort(byte[] array, int index, short value) {
        ShortVarHandle.set(array, index, value);
    }

    public static void setInt(byte[] array, int index, int value) {
        IntVarHandle.set(array, index, value);
    }

    public static void setFloat(byte[] array, int index, float value) {
        FloatVarHandle.set(array, index, value);
    }

    public static short getShort(byte[] array, int index) {
        return (short) ShortVarHandle.get(array, index);
    }

    public static int getInt(byte[] array, int index) {
        return (int) IntVarHandle.get(array, index);
    }

    public static long getLong(byte[] array, int index) {
        return (long) LongVarHandle.get(array, index);
    }

    public static float getFloat(byte[] array, int index) {
        return (float) FloatVarHandle.get(array, index);
    }
}
