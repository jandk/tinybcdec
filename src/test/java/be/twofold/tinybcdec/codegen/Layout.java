package be.twofold.tinybcdec.codegen;

import java.util.*;

interface Layout {
    List<ModeDefinition> generate();

    default boolean unpack() {
        return false;
    }

    default boolean packed() {
        return false;
    }
}
