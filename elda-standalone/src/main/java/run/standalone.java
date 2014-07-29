/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package run;

//import org.mortbay.jetty.Server;
//import org.mortbay.jetty.servlet.ServletHolder;
//import org.mortbay.jetty.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class standalone {

    public static void main( String [] args ) throws Exception {
        Server server = new Server(8080);
    //
        WebAppContext webapp = new WebAppContext( "src/main/webapp/", "/standalone" );
        webapp.addServlet(new ServletHolder( new ServletContainer( new PackagesResourceConfig("com.epimorphics.lda.restlets") ) ), "/");
        server.setHandler(webapp);
    //
        server.start();
        server.join();
    }
}
