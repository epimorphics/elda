/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.jmx;

import org.apache.jena.shared.WrappedException;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class JMXSupport {

    public static void register(String name, Object bean) {
        try {
            MBeanServer ms = ManagementFactory.getPlatformMBeanServer();
            ObjectName on = new ObjectName(name);
            ms.registerMBean(bean, on);
        } catch (Exception e) {
            throw new WrappedException(e);
        }
    }

}
