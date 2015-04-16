/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

/**
    A Couple is two things, <code>a</code> and <code>b</code>.
    It is a lightweight substitute for multiple return values.
    No clever hash, no clever equals, nada.
*/
public final class Couple<A, B> 
	{
	public final A a;
	public final B b;
	public Couple(A a, B b) { this.a = a; this.b = b; }		
	@Override public String toString() { return "(" + a + ", " + b + ")"; }
	}