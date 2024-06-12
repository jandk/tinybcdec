package be.twofold.tinybcdec;

public interface BlockDecoder {
    void decodeBlock(byte[] src, int srcPos, byte[] dst);
}
