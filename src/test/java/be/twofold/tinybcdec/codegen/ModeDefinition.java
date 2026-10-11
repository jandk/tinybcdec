package be.twofold.tinybcdec.codegen;

import java.util.*;

final class ModeDefinition {
    private final int mode;
    private final List<Endpoint> endpoints;

    ModeDefinition(int mode, List<Endpoint> endpoints) {
        this.mode = mode;
        this.endpoints = List.copyOf(endpoints);
    }

    public int mode() {
        return mode;
    }

    public List<Endpoint> endpoints() {
        return endpoints;
    }
}
