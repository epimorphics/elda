/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package run;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

public class standalone {	
	
	static final String contextPath = "/standalone";

	public static void main( String [] args ) throws Exception {  
	    Tomcat server = new Tomcat(); 
	    int port = 8080;
	    String portString = System.getProperty("elda.port");
	    if (portString != null) port = Integer.parseInt(portString);
	//
	    server.setPort(port);  
	    server.setBaseDir(".");
	    
	    String absolutePath = new File("target/elda-standalone").getAbsolutePath();
	    // System.err.println(">> " + absolutePath);
		server.addWebapp(contextPath, absolutePath);
	//
	    server.start();
	    server.getServer().await();
	} 
}
