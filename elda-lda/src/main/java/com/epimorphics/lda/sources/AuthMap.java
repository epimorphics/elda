/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.sources;

import java.io.*;
import java.util.*;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.restlets.RouterRestletSupport;
import com.epimorphics.lda.support.Glob;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.FileManager;

// EXPLORATORY.
public class AuthMap {
	
	public interface NamesAndValues {
		public String getParameter( String name );
		public List<String> getParameterNames();
	}
    protected static Logger log = LoggerFactory.getLogger(AuthMap.class);

	public static final String AUTH_NAME_PREFIX = "com.epimorphics.api.authKey";
	
	Map<String, AuthInfo> map = new HashMap<String, AuthInfo>();
	
	public static AuthMap loadAuthMap(ServletContext sc, FileManager fm, NamesAndValues map) {
		AuthMap am = new AuthMap(); 	
	//
		String contextName = RouterRestletSupport.flatContextPath(sc.getContextPath());
		String s = "/etc/elda/conf.d/{APP}/*.auth";
		String s2 = s.replaceAll( "\\{APP\\}", contextName );
		List<File> authFiles = new Glob().filesMatching(s2);
	//
		for (File af: authFiles) {
			am.put( af.getName(), readAuthFile( fm, af.toString()));
		}
	//
		return am;
	}	
	
	private void put(String name, AuthInfo ai) {
		map.put(name, ai);			
	}

	private static AuthInfo readAuthFile( FileManager fm, String fileName ) {
		
		log.debug("reading auth file '" + fileName + "'");

		String wholeFile = fm.readWholeFileAsUTF8( fileName );
		
		Properties p = new Properties();
		try {
			p.load(new StringReader(wholeFile));
		} catch (IOException e) {
			throw new WrappedException(e);
		}
		
		return new AuthInfo(p);
	}

	public AuthInfo get(String key) {
		return map.get(key);
	}
	
	@Override public String toString() {
		return map.toString();
	}
}