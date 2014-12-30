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

package com.vg.jetty.spdy.generator;

import java.nio.ByteBuffer;

import com.vg.jetty.io.ByteBufferPool;
import com.vg.jetty.spdy.frames.ControlFrame;
import com.vg.jetty.spdy.frames.WindowUpdateFrame;
import com.vg.jetty.util.BufferUtil;

public class WindowUpdateGenerator extends ControlFrameGenerator
{
    public WindowUpdateGenerator(ByteBufferPool bufferPool)
    {
        super(bufferPool);
    }

    @Override
    public ByteBuffer generate(ControlFrame frame)
    {
        WindowUpdateFrame windowUpdate = (WindowUpdateFrame)frame;

        int frameBodyLength = 8;
        int totalLength = ControlFrame.HEADER_LENGTH + frameBodyLength;
        ByteBuffer buffer = getByteBufferPool().acquire(totalLength, Generator.useDirectBuffers);
        BufferUtil.clearToFill(buffer);
        generateControlFrameHeader(windowUpdate, frameBodyLength, buffer);

        buffer.putInt(windowUpdate.getStreamId() & 0x7FFFFFFF);
        buffer.putInt(windowUpdate.getWindowDelta() & 0x7FFFFFFF);

        buffer.flip();
        return buffer;
    }
}
