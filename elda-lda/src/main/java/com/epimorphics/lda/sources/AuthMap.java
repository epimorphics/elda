/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.sources;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.support.Glob;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.FileManager;

/**
	An AuthMap is a map from authKey strings (as specified by an elda:authKey
	property of a SPARQL endpoint in an LDA config) to AuthInfo objects,
	which are themselves maps from strings (eg 'basic.password') to string
	values (eg 'mumsbirthday' or '150d7a9b34e9f'). 
*/
public class AuthMap {
	
	public final static String USUAL_AUTH_PATHS = "/etc/elda/conf.d/{APP}/*.auth";

    protected static Logger log = LoggerFactory.getLogger(AuthMap.class);
	
	final Map<String, AuthInfo> map = new HashMap<String, AuthInfo>();

	public static AuthMap loadAuthMapFromPaths(String cn, String s) {
		String expanded = s.replaceAll( "\\{APP\\}", cn );
		List<File> authFiles = new Glob().filesMatching(expanded);
	    AuthMap am = loadAuthMap( authFiles, EldaFileManager.get() );
		return am;
	}
	
	public static AuthMap readAuthMapFromPaths(AuthMap am, String cn, String s) {
		String expanded = s.replaceAll( "\\{APP\\}", cn );
		List<File> authFiles = new Glob().filesMatching(expanded);
	    AuthMap am2 = loadAuthMap( authFiles, EldaFileManager.get() );
	    am.map.putAll(am2.map);
		return am;
	}

	public static AuthMap loadAuthMap( List<File> authFiles, FileManager fm) {
		AuthMap am = new AuthMap(); 	
		for (File af: authFiles) {
			am.put( withoutSuffix(af.getName()), readAuthFile( fm, af.toString()));
		}
		return am;
	}	
	
	private static String withoutSuffix(String name) {
		int dot = name.lastIndexOf('.');
		return dot < 0 ? name : name.substring(0, dot);
	}

	private void put(String name, AuthInfo ai) {
		map.put(name, ai);			
	}

	private static AuthInfo readAuthFile( FileManager fm, String fileName ) {
		log.debug(ELog.message("reading auth file '%s'", fileName));
		return new AuthInfo(readProperties(fm, fileName));
	}

	private static Properties readProperties(FileManager fm, String fileName) {
		String wholeFile = fm.readWholeFileAsUTF8( fileName );
		Properties p = new Properties();
		try {
			p.load(new StringReader(wholeFile));
		} catch (IOException e) {
			throw new WrappedException(e);
		}
		return p;
	}

	public AuthInfo get(String key) {
		return map.get(key);
	}
	
	@Override public String toString() {
		return map.toString();
	}
}