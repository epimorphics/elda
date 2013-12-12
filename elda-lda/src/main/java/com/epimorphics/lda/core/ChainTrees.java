/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.core;

import java.util.ArrayList;

import com.epimorphics.lda.support.PrefixLogger;

@SuppressWarnings("serial") class ChainTrees extends ArrayList<ChainTree> {
	
	public void renderTriples( StringBuilder sb, PrefixLogger pl ) {
		for (ChainTree c: this) c.renderTriples( sb, pl );
	}
	
	public String renderWhere( StringBuilder sb, PrefixLogger pl, String u ) {
		for (ChainTree c: this) {
			sb.append( u );
			c.renderWhere( sb, pl );
			u = "UNION ";
		}
		return u;
	}
}