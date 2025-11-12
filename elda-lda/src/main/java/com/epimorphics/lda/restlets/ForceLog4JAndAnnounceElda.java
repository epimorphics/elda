/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import com.epimorphics.lda.Version;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForceLog4JAndAnnounceElda extends HttpServlet {

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    private static final long serialVersionUID = 1L;

    static boolean announced = false;

    @Override
    public void init() {
        if (announced == false) {
            ServletContext sc = getServletContext();
            // TODO - Replace with configuration of logback
//			String baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
//			String propertiesFile = "log4j.properties";
//
//			LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
//			File file = new File(baseFilePath + propertiesFile);
//			context.setConfigLocation(file.toURI());

            log.info("[init]\n\n    =>=> Starting Elda (Force) {}\n", Version.string);
            announced = true;
        }
    }
}
