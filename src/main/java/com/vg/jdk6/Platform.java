package com.vg.jdk6;

public class Platform {

    public static Deflater newDeflater() {
        if (IntrospectedDeflater.hasDeflateSync()) {
            return new IntrospectedDeflater();
        } else {
            return new JZlibDeflater();
        }
    }
}
