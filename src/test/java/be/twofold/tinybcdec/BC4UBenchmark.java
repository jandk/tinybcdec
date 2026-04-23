package be.twofold.tinybcdec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;
import java.nio.*;

public class BC4UBenchmark {
    @State(Scope.Thread)
    public static class BC4UState {
        private final BlockDecoder decoder;
        private final ByteBuffer src;
        private final ByteBuffer dst;

        public BC4UState() {
            try {
                decoder = BlockDecoder.bc4(false);
                src = BCTestUtils.readResource("/bc4u.dds");
                dst = ByteBuffer.allocate(256 * 256);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Benchmark
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void benchmark(BC4UState state) {
        state.decoder.decode(state.src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256, state.dst);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(BC4UBenchmark.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(opt).run();
    }
}
