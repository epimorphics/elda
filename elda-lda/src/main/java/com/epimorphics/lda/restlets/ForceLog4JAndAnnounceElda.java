/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.routing.ServletUtils;

public class ForceLog4JAndAnnounceElda extends HttpServlet{
	
    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

	private static final long serialVersionUID = 1L;

    static boolean announced = false;
    
	@Override public void init() {	
		if (announced == false) {
	    	ServletContext sc = getServletContext(); 
			String baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
			String propertiesFile = "log4j.properties";
			PropertyConfigurator.configure( baseFilePath + propertiesFile );
			log.info(ELog.message( "[init]\n\n    =>=> Starting Elda (Force) " + Version.string + "\n")); 
			announced = true;
		}
	}	
}
