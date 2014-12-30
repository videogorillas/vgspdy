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

package com.vg.jetty.npn.client;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;

import com.vg.jetty.io.ClientConnectionFactory;
import com.vg.jetty.io.Connection;
import com.vg.jetty.io.EndPoint;
import com.vg.jetty.io.NegotiatingClientConnectionFactory;
import com.vg.jetty.io.ssl.SslClientConnectionFactory;

public class NPNClientConnectionFactory extends NegotiatingClientConnectionFactory
{
    private final Executor executor;
    private final String protocol;

    public NPNClientConnectionFactory(Executor executor, ClientConnectionFactory connectionFactory, String protocol)
    {
        super(connectionFactory);
        this.executor = executor;
        this.protocol = protocol;
    }

    @Override
    public Connection newConnection(EndPoint endPoint, Map<String, Object> context) throws IOException
    {
        return new NPNClientConnection(endPoint, executor, getClientConnectionFactory(),
                (SSLEngine)context.get(SslClientConnectionFactory.SSL_ENGINE_CONTEXT_KEY), context, protocol);
    }
}
