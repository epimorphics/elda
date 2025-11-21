/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.support;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StatementTerm;
import org.apache.jena.util.iterator.SingletonIterator;

import java.util.Iterator;

/**
 * A QuerySolution with one literal binding, used as an initial setting
 * for a SPARQL query.
 *
 * @author Chris
 */
public class InitialParameter implements QuerySolution {
    private final String name;
    private final Literal literal;

    public InitialParameter(String name, Literal literal) {
        this.name = name;
        this.literal = literal;
    }

    @Override
    public String toString() {
        return "<" + name + "=" + literal + ">";
    }

    @Override
    public Iterator<String> varNames() {
        return new SingletonIterator<String>(name);
    }

    @Override
    public Resource getResource(String varName) {
        return null;
    }

    @Override
    public Literal getLiteral(String varName) {
        return (Literal) get(varName);
    }

    @Override
    public StatementTerm getStatementTerm(String varName) {
        return get(varName).asStatementTerm();
    }

    @Override
    public RDFNode get(String varName) {
        return literal;
    }

    @Override
    public boolean contains(String varName) {
        return varName.equals(name);
    }
}
    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
