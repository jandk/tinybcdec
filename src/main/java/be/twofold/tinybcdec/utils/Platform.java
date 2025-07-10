package be.twofold.tinybcdec.utils;

import java.lang.invoke.*;

public final class Platform {
    private static MethodHandle Float16ToFloatHandle;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodType type = MethodType.methodType(float.class, short.class);
        try {
            Float16ToFloatHandle = lookup.findStatic(Float.class, "float16ToFloat", type);
        } catch (ReflectiveOperationException e1) {
            try {
                Float16ToFloatHandle = lookup.findStatic(Platform.class, "float16ToFloat0", type);
            } catch (ReflectiveOperationException e2) {
                throw new ExceptionInInitializerError(e2);
            }
        }
    }

    private Platform() {
    }

    public static float float16ToFloat(short floatBinary16) {
        try {
            return (float) Float16ToFloatHandle.invokeExact(floatBinary16);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static float float16ToFloat0(short floatBinary16) {
        // Extract the separate fields
        int s = (floatBinary16 & 0x8000) << 16;
        int e = (floatBinary16 >> 10) & 0x001F;
        int m = floatBinary16 & 0x03FF;

        // Zero and denormal numbers, copies the sign
        if (e == 0) {
            float sign = Float.intBitsToFloat(s | 0x3F800000);
            return sign * (0x1p-24f * m); // Smallest denormal in float16
        }

        // Infinity and NaN, propagate the mantissa for signalling NaN
        if (e == 31) {
            return Float.intBitsToFloat(s | 0x7F800000 | m << 13);
        }

        // Adjust exponent, and put everything back together
        e += (127 - 15);
        return Float.intBitsToFloat(s | e << 23 | m << 13);
    }
}
