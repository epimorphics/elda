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
	    System.err.println(">> elda: portString=" + portString + ", port=" + port);
	//
	    server.setPort(port);  
	    server.setBaseDir(".");
	//
	    server.addWebapp(contextPath, new File("src/main/webapp").getAbsolutePath());
	//
	    server.start();
	    server.getServer().await();
	} 
}
