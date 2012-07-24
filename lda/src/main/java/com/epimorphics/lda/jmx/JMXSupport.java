package com.epimorphics.lda.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.hp.hpl.jena.shared.WrappedException;

public class JMXSupport {

	public static void register(String name, Object bean) {
		try {
			MBeanServer ms = ManagementFactory.getPlatformMBeanServer();
			ObjectName on = new ObjectName( name );
			ms.registerMBean( bean, on );
		} catch (Exception e) {
			throw new WrappedException( e );
		}
	}

}
