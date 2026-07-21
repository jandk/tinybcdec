package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;
import java.nio.*;

public class BC5UBenchmark {
    @State(Scope.Thread)
    public static class BC5UState {
        private final BlockDecoder decoder;
        private final ByteBuffer src;
        private final ByteBuffer dst;

        public BC5UState() {
            try {
                decoder = BlockDecoder.bc5(false);
                src = BCTestUtils.readResource("/bc5u.dds");
                dst = ByteBuffer.allocate(256 * 256 * BC5U.BPP);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC5UState state) {
        state.decoder.decode(state.src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256, state.dst, 256, 256);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC5UBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
