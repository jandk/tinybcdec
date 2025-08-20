package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;

public class BC6HBenchmark {
    @State(Scope.Thread)
    public static class BC6HState {
        private final BlockDecoder decoder;
        private final byte[] src;
        private final byte[] dst;

        public BC6HState() {
            try {
                decoder = BlockDecoder.bc6h(Signedness.UNSIGNED);
                src = BCTestUtils.readResource("/bc6h_uf16.dds");
                dst = new byte[256 * 256 * 6];
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC6HState state) {
        state.decoder.decode(state.src, BCTestUtils.DDS_HEADER_SIZE, 256, 256, state.dst, 0);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC6HBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
