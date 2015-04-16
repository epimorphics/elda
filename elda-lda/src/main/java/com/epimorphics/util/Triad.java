/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.util;

/**
    A Triad is three things, <code>a</code>, <code>b</code> and 
    <code>c</code>. It is a lightweight substitute for multiple 
    return values. No clever hash, no clever equals, nada.
*/
public class Triad<A, B, C>
	{
	public final A a;
	public final B b;
	public final C c;
	public Triad(A a, B b, C c) { this.a = a; this.b = b; this.c = c; }		
	@Override public String toString() { return "(" + a + ", " + b + ", " + c + ")"; }
	}
