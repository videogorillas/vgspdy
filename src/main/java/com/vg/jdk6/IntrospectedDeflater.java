package com.vg.jdk6;

import java.lang.reflect.Method;

public class IntrospectedDeflater implements Deflater {
    java.util.zip.Deflater d = new java.util.zip.Deflater();

    @Override
    public void setInput(byte[] input) {
        d.setInput(input);
    }

    @Override
    public void setDictionary(byte[] dictionary) {
        d.setDictionary(dictionary);
    }

    @Override
    public int deflate(byte[] output, int flush) {
        try {
            return (Integer) deflateSync.invoke(d, output, 0, output.length, flush);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private final static Method deflateSync = getMethod();

    private static Method getMethod() {
        try {
            return java.util.zip.Deflater.class.getDeclaredMethod("deflate", byte[].class, int.class, int.class, int.class);
        } catch (NoSuchMethodException e) {
        } catch (SecurityException e) {
        }
        return null;
    }

    public static boolean hasDeflateSync() {
        return deflateSync != null;
    }

}
