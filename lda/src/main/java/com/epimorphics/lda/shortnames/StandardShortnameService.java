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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.jsonrdf.Context.Prop;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
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
    
    protected final NameMap nameMap = new NameMap();
    
    /**
     * Construct a ShortnameService
     * @param prefixes the prefixes to use for localname disambiguation
     * @param specification the API specification with may both contain and further 
     * reference vocabulary information
     */
    public StandardShortnameService(Resource specification, PrefixMapping prefixes, ModelLoaderI loader) {
        Model specM = specification.getModel();
        PrefixMapping pm = specM;
        context = new Context(specM);
        this.prefixes = prefixes;
        for (NodeIterator i = specM.listObjectsOfProperty(specification, API.vocabulary); i.hasNext();) {
            String vocabLoc = getLexicalForm(i.next());
            Model vocab = loader.loadModel(vocabLoc);
            nameMap.load( pm, vocab );
            context.loadVocabularyAnnotations(vocab, prefixes);
        }
        nameMap.load( pm, specM );
    }
    
    /**
     * Get the prefix/URI mapping defined for this API
     */
    public PrefixMapping getPrefixes() {
        return prefixes;
    }
    
    /**
        Answer the NameMap of this Shortname Service.
    */
    @Override public NameMap nameMap() {
    	return nameMap;
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
     * Convert a resource specification to a resource. A resource may be specified 
     * by a shortname in a query string or a literal giving a shortname in a spec
     * file as well as simply being a resource already.
     */
    @Override public Resource asResource( RDFNode res ) {
        if (res instanceof Resource) {
            return (Resource)res;
        } else if (res instanceof Literal) {
            return asResource( ((Literal)res).getLexicalForm() );
        }
        throw new ExpansionFailedException( res );
    }
	
    @Override public Resource asResource( String res ) {
        String uri = expand( res );
        if (uri == null &&  RDFUtil.looksLikeURI(res)) {
            uri = res;
        }
        if (uri != null)
            return ResourceFactory.createResource(uri);
        throw new ExpansionFailedException( res );
    }
    
    /**
     * Return a Context object suitable for driving the JSON encoding
     */
    @Override public Context asContext() {
        return context;
    }
    
    /**
        Normalise a property-valueString pair with a given language to an
        RDFQ node which has the spelling from valueString and is typed
        according to the property type.
    */
    @Override public Any valueAsRDFQ( String p, String nodeValue, String language ) {
		if (nodeValue.startsWith("?"))
            return RDFQ.var( nodeValue );
        String full = expand( nodeValue );
        Prop prop = context.getPropertyByName( p );
        if (full != null) 
            return RDFQ.uri(full); 
        if (prop == null) {
            if (RDFUtil.looksLikeURI( nodeValue )) {
                return RDFQ.uri( nodeValue ); 
            }
        } else {
            String type = prop.getType();
            if (type != null) {
                if (type.equals(OWL.Thing.getURI()) || type.equals(RDFS.Resource.getURI())) {
                    return RDFQ.uri(nodeValue); 
                } else if (isDatatype( type )) {
                	RDFDatatype dt = TypeMapper.getInstance().getTypeByName(type);
                	if (dt == null) dt = fakeDatatype( type );
                	if (!dt.getURI().equals( RDFUtil.RDFPlainLiteral ))
                		return RDFQ.literal( nodeValue, null, dt.getURI() );
                }
            }
        }
        return RDFQ.literal( nodeValue, language, "" );
	}
    
    protected final Set<String> datatypes = new HashSet<String>();
    
    public void declareDatatype( String type ) {
    	datatypes.add( type );
    }
    
    static final boolean everythingIsADatatype = false;
    
    private boolean isDatatype( String type ) {
    	if (everythingIsADatatype) return true;
    	if (datatypes.contains( type )) return true;
    	if (type.startsWith( XSD.getURI() )) return true;
    	return false;
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
}

