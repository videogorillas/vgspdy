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

package com.vg.jetty.spdy.client;

import java.io.IOException;
import java.util.Map;

import com.vg.jetty.io.ByteBufferPool;
import com.vg.jetty.io.ClientConnectionFactory;
import com.vg.jetty.io.Connection;
import com.vg.jetty.io.EndPoint;
import com.vg.jetty.spdy.CompressionFactory;
import com.vg.jetty.spdy.FlowControlStrategy;
import com.vg.jetty.spdy.StandardCompressionFactory;
import com.vg.jetty.spdy.StandardSession;
import com.vg.jetty.spdy.api.Session;
import com.vg.jetty.spdy.api.SessionFrameListener;
import com.vg.jetty.spdy.client.SPDYClient.Factory;
import com.vg.jetty.spdy.generator.Generator;
import com.vg.jetty.spdy.parser.Parser;
import com.vg.jetty.util.Promise;

public class SPDYClientConnectionFactory implements ClientConnectionFactory
{
    public static final String SPDY_CLIENT_CONTEXT_KEY = "spdy.client";
    public static final String SPDY_SESSION_LISTENER_CONTEXT_KEY = "spdy.session.listener";
    public static final String SPDY_SESSION_PROMISE_CONTEXT_KEY = "spdy.session.promise";

    @Override
    public Connection newConnection(EndPoint endPoint, Map<String, Object> context) throws IOException
    {
        SPDYClient client = (SPDYClient)context.get(SPDY_CLIENT_CONTEXT_KEY);
        SPDYClient.Factory factory = client.getFactory();
        ByteBufferPool byteBufferPool = factory.getByteBufferPool();
        CompressionFactory compressionFactory = new StandardCompressionFactory();
        Parser parser = new Parser(compressionFactory.newDecompressor());
        Generator generator = new Generator(byteBufferPool, compressionFactory.newCompressor());

        SPDYConnection connection = new ClientSPDYConnection(endPoint, byteBufferPool, parser, factory, client.isDispatchIO());

        FlowControlStrategy flowControlStrategy = client.newFlowControlStrategy();

        SessionFrameListener listener = (SessionFrameListener)context.get(SPDY_SESSION_LISTENER_CONTEXT_KEY);
        StandardSession session = new StandardSession(client.getVersion(), byteBufferPool,
                factory.getScheduler(), connection, endPoint, connection, 1, listener, generator, flowControlStrategy);

        session.setWindowSize(client.getInitialWindowSize());
        parser.addListener(session);
        connection.setSession(session);

        @SuppressWarnings("unchecked")
        Promise<Session> promise = (Promise<Session>)context.get(SPDY_SESSION_PROMISE_CONTEXT_KEY);
        promise.succeeded(session);

        return connection;
    }

    private class ClientSPDYConnection extends SPDYConnection
    {
        private final Factory factory;

        public ClientSPDYConnection(EndPoint endPoint, ByteBufferPool bufferPool, Parser parser, Factory factory, boolean dispatchIO)
        {
            super(endPoint, bufferPool, parser, factory.getExecutor(), dispatchIO);
            this.factory = factory;
        }

        @Override
        public void onOpen()
        {
            super.onOpen();
            factory.sessionOpened(getSession());
        }

        @Override
        public void onClose()
        {
            super.onClose();
            factory.sessionClosed(getSession());
        }
    }
}
