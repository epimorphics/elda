package com.epimorphics.lda.systemtest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {
	private static final String CONFIG_PROPERTIES_PATH_NAME = "com.epimorphics.lda.systemtest.config.properties";
	static Properties properties;
	
	public static Properties getProperties() {
		return properties;
	}
	
    private static final Logger logger = Logger.getLogger(Config.class);
	
	static {		
		properties = new Properties();
		InputStream istream = Config.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES_PATH_NAME);
		if (istream != null) {
			try {
				properties.load(istream);
			} catch (IOException e) {
				throw new Error("failed to read config properties file: " + CONFIG_PROPERTIES_PATH_NAME, e);
			}
		} else {
			logger.warn("failed to read configuration properties file: " + CONFIG_PROPERTIES_PATH_NAME);
		}
	}
}
