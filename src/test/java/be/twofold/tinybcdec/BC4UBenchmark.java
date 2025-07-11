package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;

public class BC4UBenchmark {
    @State(Scope.Thread)
    public static class BC4UState {
        private final BlockDecoder decoder;
        private final byte[] src;
        private final byte[] dst;

        public BC4UState() {
            try {
                decoder = BlockDecoder.bc4(false);
                src = BCTestUtils.readResource("/bc4u.dds");
                dst = new byte[256 * 256];
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC4UState state) {
        state.decoder.decode(256, 256, state.src, BCTestUtils.DDS_HEADER_SIZE, state.dst, 0);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC4UBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
