/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        ShortnameService.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.shortnames;
import static com.epimorphics.util.RDFUtils.*;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.jsonrdf.Context.Prop;
import com.epimorphics.lda.core.APIException;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.LiteralNode;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.Term;
import com.epimorphics.lda.rdfq.URINode;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.*;

/**
	See ShortnameService.
	
 	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class StandardShortnameService implements ShortnameService {

    protected Context context;
    protected PrefixMapping prefixes;
    
    /**
     * Construct a ShortnameService
     * @param prefixes the prefixes to use for localname disambiguation
     * @param specification the API specification with may both contain and further 
     * reference vocabulary information
     */
    public StandardShortnameService(Resource specification, PrefixMapping prefixes, ModelLoaderI loader) {
        Model specM = specification.getModel();
        context = new Context(specM);
        this.prefixes = prefixes;
        for (NodeIterator i = specM.listObjectsOfProperty(specification, API.vocabulary); i.hasNext();) {
            String vocabLoc = getLexicalForm(i.next());
            Model vocab = loader.loadModel(vocabLoc);
//            Model vocab = FileManager.get().loadModel(vocabLoc);
            context.loadVocabularyAnnotations(vocab, prefixes);
        }
    }
    
    /**
     * Get the prefix/URI mapping defined for this API
     */
    public PrefixMapping getPrefixes() {
        return prefixes;
    }
    
    /**
     * Find the file URI for the given shortname
     */
    @Override public String expand(String shortname) {
        String uri = context.getURIfromName(shortname);
        if (uri == null && shortname.contains("_")) {
            // check for prefix form
            String pform = shortname.replaceFirst("_", ":");
            String exp = prefixes.expandPrefix(pform);
            if ( ! exp.equals(pform)) 
                uri = exp;
        }
        return uri;
    }
    
    /**
     * Find the preferred shortname for a given URI.
     * Note that shorten(expand(s)) may not yield s in the cases
     * where s is a valid but non-preferred shortname.
     */
    @Override public String shorten(String uri) {
        return context.getNameForURI(uri);
    }

    /**
     * Convert a resource specification to a resource. A resource may be specified 
     * by a shortname in a query string or a literal giving a shortname in a spec
     * file as well as simply being a resource already.
     */
    @Override public Resource normalizeResource(RDFNode res) {
        if (res instanceof Resource) {
            return (Resource)res;
        } else if (res instanceof Literal) {
            return normalizeResource( ((Literal)res).getLexicalForm() );
        }
        throw new APIException("Failed to expand resource: " + res);
    }

	@Override public Resource normalizeResource( Term r ) {
		if (r instanceof URINode) return ResourceFactory.createResource( r.spelling() );
		if (r instanceof LiteralNode) return normalizeResource( r.spelling() );
        throw new APIException( "Failed to expand resource: " + r );
	}
	
    @Override public Resource normalizeResource(String res) {
        String uri = expand( res );
        if (uri == null &&  RDFUtil.looksLikeURI(res)) {
            uri = res;
        }
        if (uri != null)
            return ResourceFactory.createResource(uri);
        throw new APIException("Failed to expand resource: " + res);
    }
    
    /**
     * Return a Context object suitable for driving the JSON encoding
     */
    public Context asContext() {
        return context;
    }
    
    /**
     * Normalize a node value, using the given property name as a guide
     * to typing information. If the value is a variable then leave it as such.
     */
    private Node normalizeNode(String p, String nodeValue) {
        if (nodeValue.startsWith("?")) {
            return Node.createVariable(nodeValue.substring(1));
        } else {
            String full = expand(nodeValue);
            if (full != null) {
                // Able to expand so assume it is a resource and now have the URI
                return Node.createURI(full); 
            } else {
                Prop prop = context.getPropertyByName(p);
//                System.err.println( ">> property information for " + p + " is " + prop );
//                System.err.println( ">> (URI: " + context.getURIfromName( p ) + ")" );
                if (prop == null) {
                    // No typing information 
                    if (RDFUtil.looksLikeURI(nodeValue)) {
                        return Node.createURI(nodeValue); 
                    } else {
                        return Node.createLiteral(nodeValue);
                    }
                } else {
                    String type = prop.getType();
                    if (type != null) {
                        if (type.equals(OWL.Thing.getURI()) || type.equals(RDFS.Resource.getURI())) {
                            return Node.createURI(nodeValue); 
                        } else {
                        	RDFDatatype dt = TypeMapper.getInstance().getTypeByName(type);
                        	if (dt == null) dt = fakeDatatype( type );
                        	if (dt.getURI().equals( RDFUtil.RDFPlainLiteral ))
                        		return Node.createLiteral( nodeValue );
                        	else
                        		return Node.createLiteral( nodeValue, null, dt );
                        }
                    } else {
                        return Node.createLiteral(nodeValue);
                    }
                    // TODO other type cases
                }
            }
        }
    }
    
    private final Map<String, RDFDatatype> fakeTypes = new HashMap<String, RDFDatatype>();
    
    private RDFDatatype fakeDatatype( String type ) {
    	RDFDatatype result = fakeTypes.get( type );
    	if (result == null) fakeTypes.put( type, result = new FakeDatatype( type ) );
		return result;
	}
    
    static private class FakeDatatype extends BaseDatatype {	
    	public FakeDatatype( String type ) {
    		super( type );
    	}
    	
    	@Override public boolean isValidValue( Object value )
    		{ throw new RuntimeException( "unimplemented: isValidValue of fake RDF datatype for " + uri ); }
    	
    	@Override public Class<?> getJavaClass() 
    		{ throw new RuntimeException( "unimplemented: getJavaClass of fake RDF datatype for " + uri ); }
    	
    	@Override public Object parse( String lexicalForm ) 
    		//{ throw new IllegalArgumentException( "don't know how to parse a " + uri ); }
    		{ return lexicalForm; }
    	
    	@Override public String toString() 
    		{ return "Datatype[fake: " + uri + "]"; }
    }

	/**
     * Normalize a node value, using the given property name as a guide
     * to typing information. If the value is a variable then leave it as such.
     * @return a string suitable for embedding in a SPARQL query
     */
    public String normalizeNodeToString(String p, String nodeValue) {
    	return normalizeNodeToString(p, nodeValue, null);
    }
    
    public Any normalizeNodeToRDFQ( String p, String nodeValue, String language ) {
        Node n = normalizeNode( p, nodeValue );
    	if (n.isURI()) return RDFQ.uri( n.getURI() );
    	if (n.isVariable()) return RDFQ.var( "?" + n.getName() );
    	if (n.isLiteral()) return RDFQ.literal
    		( n.getLiteralLexicalForm(), language, n.getLiteralDatatypeURI() );
    	throw new IllegalArgumentException( "cannot normalise: " + n.toString() );
    }
    
    /**
     * Normalize a node value, using the given property name as a guide
     * to typing information. If the value is a variable then leave it as such.
     * @return a string suitable for embedding in a SPARQL query
     */
    public String normalizeNodeToString(String p, String nodeValue, String language) {
        Node n = normalizeNode(p, nodeValue);
        if (n.isURI()) {
            return "<" + n.getURI() + ">";
        } else if (n.isLiteral()) {
            String dt = n.getLiteralDatatypeURI();
            if (dt != null && !dt.isEmpty()) {
                return "'" + nodeValue +"'^^<" + dt + ">";
            } else {
                return "'" + nodeValue + (language == null ? "'" : "'@" + language);
            }
        }
        return n.toString();
    }
    
    /**
     * Normalize a simple untyped string, if it is a known resource
     * then return as <uri> string else quote it for insertion in a query.
     * TODO Review how to add typing information to this.
     */
    public String normalizeValue(String val) {
    	return normalizeValue(val, null);
    }
    
    /**
     * Normalize a simple untyped string, if it is a known resource
     * then return as <uri> string else quote it for insertion in a query.
     * TODO Review how to add typing information to this.
     */
    public String normalizeValue(String val, String language) {
        String uri = expand(val);
        if (uri == null) {
        	try {
        		Integer.parseInt(val);
        		return val ;
        	} catch (NumberFormatException e) {
        		return "'" + val +  (language == null ? "'" : "'@" + language);
        	}
        } else {
            return "<" + uri + ">";
        }
    }

}

