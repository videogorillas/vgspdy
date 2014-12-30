package com.vg.jdk6;

public interface Deflater {

    public final static int NO_FLUSH = 0;
    public final static int SYNC_FLUSH = 2;

    void setInput(byte[] input);

    void setDictionary(byte[] dictionary);

    int deflate(byte[] output, int flush);

}
