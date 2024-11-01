package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;

public class BC7Benchmark {
    @State(Scope.Thread)
    public static class BC7State {
        private final BlockDecoder decoder;
        private final byte[] src;
        private final byte[] dst;

        public BC7State() {
            try {
                decoder = BlockDecoder.create(BlockFormat.BC7, PixelOrder.RGBA);
                src = BCTestUtils.readResource("/bc7.dds");
                dst = new byte[256 * 256 * 4];
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC7State state) {
        state.decoder.decode(256, 256, state.src, BCTestUtils.DDS_HEADER_SIZE, state.dst, 0);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC7Benchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
