package com.epimorphics.lda.routing;

public class Container {
	
	protected static final long serialVersionUID = 1L;

	/**
	    Prefix on a config name that forces it to be interpreted as a local
	    filename.
	*/
	public static final String LOCAL_PREFIX = "local:";

	public static final String LOG4J_PARAM_NAME = "log4j-init-file";

	/**
		When the Loader class is initialised, if ELDA_SPEC_SYSTEM_PROPERTY_NAME
		is bound to one or more values, those values are used as config file
		names rather than using the default names in web.xml.
	*/
	public static final String ELDA_SPEC_SYSTEM_PROPERTY_NAME = "elda.spec";

	/**
	 	The param-name for the elda config files wired into web.xml.
	*/
	public static final String INITIAL_SPECS_PARAM_NAME = "com.epimorphics.api.initialSpecFile";
	
	/**
	    The param-name for a prefix-path to be applied to all uriTemplates
	    in configs (unless overridden by the prefix:: syntax). By default
	    the prefix is empty.
	*/
	public static final String INITIAL_SPECS_PREFIX_PATH_NAME = "com.epimorphics.api.prefixPath";

}
