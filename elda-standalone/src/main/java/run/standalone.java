/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package run;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class standalone {

	static final String contextPath = "/standalone";

	public static void main( String [] args ) throws Exception {  		
	    Tomcat server = new Tomcat(); 
	    server.setPort(8080);
	//	    
	    server.setBaseDir(".");
	    PackagesResourceConfig p = new PackagesResourceConfig("com.epimorphics.lda.restlets");
	    ServletContainer c = new ServletContainer(p);
	//
	    Context cx = server.addContext(contextPath, "GOOPSTY");
	    Tomcat.addServlet(cx, contextPath, c);
    //
	    server.start();
	    server.getServer().await();
	} 
}
