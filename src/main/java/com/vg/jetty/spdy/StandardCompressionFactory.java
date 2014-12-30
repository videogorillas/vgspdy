//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.vg.jetty.spdy;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

import com.vg.jdk6.Deflater;
import com.vg.jdk6.Platform;

public class StandardCompressionFactory implements CompressionFactory {
    @Override
    public Compressor newCompressor() {
        return new StandardCompressor();
    }

    @Override
    public Decompressor newDecompressor() {
        return new StandardDecompressor();
    }

    public static class StandardCompressor implements Compressor {
        private final Deflater deflater = Platform.newDeflater();

        @Override
        public void setInput(byte[] input) {
            deflater.setInput(input);
        }

        @Override
        public void setDictionary(byte[] dictionary) {
            deflater.setDictionary(dictionary);
        }

        @Override
        public int compress(byte[] output) {
            return deflater.deflate(output, Deflater.SYNC_FLUSH);
        }
    }

    public static class StandardDecompressor implements CompressionFactory.Decompressor {
        private final Inflater inflater = new Inflater();

        @Override
        public void setDictionary(byte[] dictionary) {
            inflater.setDictionary(dictionary);
        }

        @Override
        public void setInput(byte[] input) {
            inflater.setInput(input);
        }

        @Override
        public int decompress(byte[] output) throws ZipException {
            try {
                return inflater.inflate(output);
            } catch (DataFormatException x) {
                throw (ZipException) new ZipException().initCause(x);
            }
        }
    }
}
