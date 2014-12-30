package com.vg.jdk6;

import java.io.Closeable;

public interface AutoCloseable extends Closeable {
    void close();

}
