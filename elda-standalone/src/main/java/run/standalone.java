package run;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

public class standalone {	
	
	static final String contextPath = "/standalone";

	public static void main( String [] args ) throws Exception {  
		
	    Tomcat server = new Tomcat(); 
	    server.setPort(8080);
	//	    
	    server.setBaseDir(".");
	
	    server.addWebapp(contextPath,  new File("src/main/webapp").getAbsolutePath());
	    
	//    PackagesResourceConfig p = new PackagesResourceConfig("com.epimorphics.lda.restlets");
	//    ServletContainer c = new ServletContainer(p);
	////
	//    Context cx = server.addContext(contextPath, "GOOPSTY");
	//    Tomcat.addServlet(cx, contextPath, c);
	////
	    server.start();
	    server.getServer().await();
	} 

}
