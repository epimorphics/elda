package com.epimorphics.lda.specs;

import java.util.*;

import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class PropertyExpiryTimes {

	protected final Map<Node, Long> bindings = new HashMap<Node, Long>();
	
	protected long minTime = Long.MAX_VALUE;
	
	public PropertyExpiryTimes() {
	}
	
	public static PropertyExpiryTimes testAssembly(Object ... args) {
		PropertyExpiryTimes result = new PropertyExpiryTimes();
		for (int i = 0; i < args.length; i += 2)
			result.put( ((Resource) args[i]).asNode(), (Long) args[i+1] );
		return result;
	}

	public static PropertyExpiryTimes assemble(Model model) {
		PropertyExpiryTimes result = new PropertyExpiryTimes();
		List<Statement> candidates = model
			.listStatements(null, EXTRAS.cacheExpiryTime, (RDFNode) null)
			.toList()
			;
		for (Statement c: candidates)
			if (isProperty( c.getSubject())) {
				long t = getSecondsValue(c.getSubject(), EXTRAS.cacheExpiryTime, -1);
				result.put(c.getSubject().asNode(), t);
			}
				
		return result;
	}
	
	protected void put(Node n, long t) {
		bindings.put(n, t);
		if (t < minTime) minTime = t;
	}
	
	private static boolean isProperty(Resource s) {
		return 
			s.hasProperty(RDF.type, RDF.Property)
			|| s.hasProperty(RDF.type, OWL.DatatypeProperty)
			|| s.hasProperty(RDF.type, OWL.ObjectProperty)
			;
	}

	@Override public String toString() {
		return "<PET " + bindings + ">";
	}
	
	@Override public boolean equals( Object other ) {
		return 
			other instanceof PropertyExpiryTimes 
			&& same( (PropertyExpiryTimes) other )
			;
	}

	private boolean same(PropertyExpiryTimes other) {
		return bindings.equals(other.bindings);
	}

	public static long getSecondsValue(Resource x, Property p, long ifAbsent) {
		Statement s = x.getProperty( p );
		if (s == null) return ifAbsent;
		RDFNode n = s.getObject();
		if (n.isResource()) return ifAbsent;
		if (n.asLiteral().getDatatypeURI() == null) {
			String spelling = n.asLiteral().getLexicalForm();
			char last = spelling.charAt(spelling.length() - 1);
			if (Character.isDigit(last)) {
				return Long.parseLong(spelling);
			} else {
				long l = Long.parseLong(spelling.substring(0, spelling.length() - 1));
				return l * scale(last);
			}
		} else {
			return n.asLiteral().getLong();
		}
	}
    
    static long scale(char last) {
		if (last == 's') return 1;
		if (last == 'm') return 60;
		if (last == 'h') return 60 * 60;
		if (last == 'd') return 60 * 60 * 24;
		if (last == 'w') return 60 * 60 * 24 * 7;
		return 1;
	}
    
    public long timeFor(Resource r) {
    	Long t = bindings.get(r.asNode());
    	return t == null ? Long.MAX_VALUE : t;
    }
    
    public long minTime() {
    	System.err.println( ">> minTime " + minTime + " for " + bindings );
    	return minTime;
    }

    /**
        Return the minimum expiry time (duration) for this object model
    */
	public long minExpiryTime(Model dataModel, long givenDuration) {
		Graph dataGraph = dataModel.getGraph();
		long modelDuration = Long.MAX_VALUE;
		for (Map.Entry<Node, Long> e: bindings.entrySet())
			if (dataGraph.contains(Node.ANY, e.getKey(), Node.ANY)) {
				if (e.getValue() < modelDuration) modelDuration = e.getValue();
			}
	//
		if (modelDuration == Long.MAX_VALUE) return givenDuration;
		if (givenDuration < 0) return modelDuration;
		return Math.min(modelDuration, givenDuration);
	}
}
