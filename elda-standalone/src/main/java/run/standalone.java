/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package run;

import java.io.File;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class standalone {	
	
	static final String standalone = "/standalone";
	static final String ROOT = "/";

	public static void main( String [] args ) throws Exception {  
	    Tomcat server = new Tomcat(); 
	    int port = 8080;
	    String portString = System.getProperty("elda.port");
	    if (portString != null) port = Integer.parseInt(portString);

        Connector connector = new Connector();
        connector.setPort(port);
        server.getService().addConnector(connector);

	    server.setBaseDir(".");
	    
//	    System.err.println(">> " + new File(".").getAbsolutePath());
//	    String absolutePath = new File("target/elda-standalone").getAbsolutePath();
	    String absolutePath = new File("src/main/webapp").getAbsolutePath();
	    // System.err.println(">> " + absolutePath);
		server.addWebapp(standalone, absolutePath);
		server.addWebapp(ROOT, absolutePath);
	//
	    server.start();
	    server.getServer().await();
	} 
}
