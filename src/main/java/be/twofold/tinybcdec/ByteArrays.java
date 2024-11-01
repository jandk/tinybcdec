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

    private ByteArrays() {
    }

    static void setShort(byte[] array, int index, short value) {
        ShortVarHandle.set(array, index, value);
    }

    static void setInt(byte[] array, int index, int value) {
        IntVarHandle.set(array, index, value);
    }

    static void setLong(byte[] array, int index, long value) {
        LongVarHandle.set(array, index, value);
    }

    static long getLong(byte[] array, int index) {
        return (long) LongVarHandle.get(array, index);
    }
}
