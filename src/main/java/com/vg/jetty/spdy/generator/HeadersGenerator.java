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
import com.vg.jetty.spdy.SessionException;
import com.vg.jetty.spdy.api.SPDY;
import com.vg.jetty.spdy.api.SessionStatus;
import com.vg.jetty.spdy.frames.ControlFrame;
import com.vg.jetty.spdy.frames.HeadersFrame;
import com.vg.jetty.util.BufferUtil;

public class HeadersGenerator extends ControlFrameGenerator
{
    private final HeadersBlockGenerator headersBlockGenerator;

    public HeadersGenerator(ByteBufferPool bufferPool, HeadersBlockGenerator headersBlockGenerator)
    {
        super(bufferPool);
        this.headersBlockGenerator = headersBlockGenerator;
    }

    @Override
    public ByteBuffer generate(ControlFrame frame)
    {
        HeadersFrame headers = (HeadersFrame)frame;
        short version = headers.getVersion();

        ByteBuffer headersBuffer = headersBlockGenerator.generate(version, headers.getHeaders());

        int frameBodyLength = 4;
        if (frame.getVersion() == SPDY.V2)
            frameBodyLength += 2;

        int frameLength = frameBodyLength + headersBuffer.remaining();
        if (frameLength > 0xFFFFFF)
        {
            // Too many headers, but unfortunately we have already modified the compression
            // context, so we have no other choice than tear down the connection.
            throw new SessionException(SessionStatus.PROTOCOL_ERROR, "Too many headers");
        }

        int totalLength = ControlFrame.HEADER_LENGTH + frameLength;

        ByteBuffer buffer = getByteBufferPool().acquire(totalLength, Generator.useDirectBuffers);
        BufferUtil.clearToFill(buffer);
        generateControlFrameHeader(headers, frameLength, buffer);

        buffer.putInt(headers.getStreamId() & 0x7FFFFFFF);
        if (frame.getVersion() == SPDY.V2)
            buffer.putShort((short)0);

        buffer.put(headersBuffer);

        buffer.flip();
        return buffer;
    }
}
