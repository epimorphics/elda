/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.epimorphics.lda.Version;
import com.epimorphics.lda.routing.ServletUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ForceLog4JAndAnnounceElda extends HttpServlet {

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    private static final long serialVersionUID = 1L;

    static boolean announced = false;

    @Override
    public void init() {
        if (!announced) {
            ServletContext sc = getServletContext();

			String baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
			File propertiesFile = new File(baseFilePath + "logback.xml");
            if (propertiesFile.exists()) {
                configureLogging(propertiesFile);
            }
            log.info("[init]\n\n    =>=> Starting Elda (Force) {}\n", Version.string);
            announced = true;
        }
    }

    private void configureLogging(File propertiesFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        try (InputStream configStream = FileUtils.openInputStream(propertiesFile)) {
            configurator.setContext(loggerContext);
            configurator.doConfigure(configStream); // loads logback file
        } catch (IOException ex) {
            log.warn("Failed to load properties file: " + propertiesFile.getAbsolutePath(), ex);
        } catch (JoranException ex) {
            log.warn("Failed to configure logback with file: " + propertiesFile.getAbsolutePath(), ex);
        }
    }
}
