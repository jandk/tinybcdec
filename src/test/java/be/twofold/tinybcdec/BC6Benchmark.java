package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;

public class BC6Benchmark {
    @State(Scope.Thread)
    public static class BC6State {
        private final BlockDecoder decoder;
        private final byte[] src;
        private final byte[] dst;

        public BC6State() {
            try {
                decoder = BlockDecoder.create(BlockFormat.BC6Unsigned, PixelOrder.RGB);
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
    public void benchmark(BC6State state) {
        state.decoder.decode(256, 256, state.src, BCTestUtils.DDS_HEADER_SIZE, state.dst, 0);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC6Benchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
