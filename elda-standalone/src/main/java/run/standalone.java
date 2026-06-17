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
import org.apache.jena.riot.RIOT;

public class standalone {

	public static void main( String [] args ) throws Exception {
		RIOT.init();
	    Tomcat server = new Tomcat();
	    String portString = System.getProperty ("elda_port");
		int port = portString != null ? Integer.parseInt(portString) : 8080;

		String ctxPath = System.getenv("elda_context_path");
		ctxPath = ctxPath != null ? ctxPath : "/standalone";

		String basePath = System.getenv("elda_webapp_path");
		basePath = basePath != null ? basePath : "src/main/webapp";

        Connector connector = new Connector();
        connector.setPort(port);
        server.getService().addConnector(connector);
	    server.setBaseDir(".");

	    String absolutePath = new File(basePath).getAbsolutePath();
		server.addWebapp(ctxPath, absolutePath);

		server.getConnector();
	    server.start();
	    server.getServer().await();
	} 
}
