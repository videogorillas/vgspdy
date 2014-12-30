package com.vg.jdk6;

import com.jcraft.jzlib.JZlib;

public class JZlibDeflater implements Deflater {
    private com.jcraft.jzlib.Deflater jzlib;

    public JZlibDeflater() {
        jzlib = new com.jcraft.jzlib.Deflater();
        jzlib.init(JZlib.Z_DEFAULT_COMPRESSION);
    }

    @Override
    public void setInput(byte[] input) {
        jzlib.setInput(input);
    }

    @Override
    public void setDictionary(byte[] dictionary) {
        jzlib.setDictionary(dictionary, dictionary.length);
    }

    @Override
    public int deflate(byte[] output, int flush) {
        jzlib.setOutput(output);
        jzlib.deflate(flush);
        return output.length - jzlib.avail_out;
    }

}
