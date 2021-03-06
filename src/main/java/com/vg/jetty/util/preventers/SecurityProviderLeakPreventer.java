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

package com.vg.jetty.util.preventers;

import java.security.Security;

/**
 * SecurityProviderLeakPreventer
 *
 * Some security providers, such as sun.security.pkcs11.SunPKCS11 start a deamon thread,
 * which will use the thread context classloader. Load them here to ensure the classloader
 * is not a webapp classloader.
 *
 * Inspired by Tomcat JreMemoryLeakPrevention
 */
public class SecurityProviderLeakPreventer extends AbstractLeakPreventer
{
    /* ------------------------------------------------------------ */
    /** 
     * @see com.vg.jetty.util.preventers.AbstractLeakPreventer#prevent(java.lang.ClassLoader)
     */
    @Override
    public void prevent(ClassLoader loader)
    {
        Security.getProviders();
    }

}
