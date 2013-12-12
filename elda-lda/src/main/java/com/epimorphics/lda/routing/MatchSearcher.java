/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.routing;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.support.MultiMap;

/**
    A MatchSearcher<T> maintains a collection of MatchTemplate<T>s.
    The collection can be added to and removed from [TBD]. It can be 
    searched for an entry matching a supplied path; if there is one, 
    bindings are updated and an associated value returned.

    @author eh
*/
public class MatchSearcher<T> {
    
    List<MatchTemplate<T>> templates = new ArrayList<MatchTemplate<T>>();
    boolean needsSorting = false;
    
    static final Logger log = LoggerFactory.getLogger( MatchSearcher.class );
    
    /**
        Add the template <code>path</code> to the collection, associated
        with the supplied result value.
    */
    public void register( String path, T result ) {
    	log.info( "registering " + path + " for " + result.toString() );
        templates.add( MatchTemplate.prepare( path, result ) );
        needsSorting = true;
    }

    /**
        Remove the entry with the given template path from
        the collection.
    */
    public void unregister( String path ) {
    	String trimmedPath = removeQueryPart( path );
        Iterator<MatchTemplate<T>> it = templates.iterator();
        while (it.hasNext()) {        	
            String t = it.next().template();
			if (t.equals( trimmedPath )) 
                { it.remove(); return; }
        }
    }
    
    private String removeQueryPart( String path ) {
    	int qPos = path.indexOf('?');
		return qPos < 0 ? path : path.substring( 0, qPos );
	}

	/**
        Search the collection for the most specific entry that
        matches <code>path</code>. If there isn't one, return null.
        If there is, return the associated value, and update the
        bindings with the matches variables.
    */
    public T lookup( Map<String, String> bindings, String path, MultiMap<String, String> queryParams ) {
        if (needsSorting) sortTemplates();    
        for (MatchTemplate<T> t: templates) {
        	if (t.match( bindings, path, queryParams )) return t.value();
        }
        return null;
    }

    private void sortTemplates() {
        Collections.sort( templates, MatchTemplate.compare );
        needsSorting = false;
    }
    
    public List<String> templates() {
    	List<String> result = new ArrayList<String>();
    	if (needsSorting) sortTemplates();
    	for (MatchTemplate<?> mt: templates) result.add(mt.template());
    	return result;
    }
}