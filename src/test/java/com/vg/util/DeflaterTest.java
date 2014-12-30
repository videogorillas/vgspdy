package com.vg.util;

import static org.junit.Assert.*;

import java.util.zip.Deflater;

import org.junit.Test;

import com.vg.jetty.spdy.CompressionDictionary;

public class DeflaterTest {
    @Test
    public void testJdk6() throws Exception {
        byte[] bs = CompressionDictionary.get((short) 3);
        Deflater d = new Deflater();
        d.setInput(new byte[]{1,2,3});
        d.deflate(new byte[10]);
        d.setDictionary(bs);
        d.setDictionary(bs);
    }
}
