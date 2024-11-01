package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;

public class BC2Benchmark {
    @State(Scope.Thread)
    public static class BC2State {
        private final BlockDecoder decoder;
        private final byte[] src;
        private final byte[] dst;

        public BC2State() {
            try {
                decoder = BlockDecoder.create(BlockFormat.BC2);
                src = BCTestUtils.readResource("/bc2.dds");
                dst = new byte[256 * 256 * 4];
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC2State state) {
        state.decoder.decode(256, 256, state.src, BCTestUtils.DDS_HEADER_SIZE, state.dst, 0);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC2Benchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
