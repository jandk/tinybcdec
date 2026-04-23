package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;
import java.nio.*;

public class BC1Benchmark {
    @State(Scope.Thread)
    public static class BC1State {
        private final BlockDecoder decoder;
        private final ByteBuffer src;
        private final ByteBuffer dst;

        public BC1State() {
            try {
                decoder = BlockDecoder.bc1(true);
                src = BCTestUtils.readResource("/bc1.dds");
                dst = ByteBuffer.allocate(256 * 256 * 4);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC1State state) {
        state.decoder.decode(state.src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256, state.dst);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC1Benchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
