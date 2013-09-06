package run;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

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
