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

package com.vg.jetty.util.resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Java NIO Path equivalent of FileResource.
 */
public class PathResource extends FileResource
{

    public PathResource(File file) {
        super(file);
    }

    public PathResource(URL url) throws IOException, URISyntaxException {
        super(url);
    }
}
