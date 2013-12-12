/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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
import static com.epimorphics.util.RDFUtils.getLexicalForm;

import java.util.*;

import com.epimorphics.jsonrdf.*;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.UnknownShortnameException;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.*;

/**
	See ShortnameService.
	
 	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class StandardShortnameService implements ShortnameService {

    protected final Context context;
    protected final PrefixMapping prefixes;
    
    /**
     	Initialise a ShortnameService
     	@param specRoot the API specification 
     	@param prefixes prefixes to use
     	@param loader the loader to use for vocabularies
    */
    public StandardShortnameService( Resource specRoot, PrefixMapping prefixes, ModelLoader loader ) {
    	this.prefixes = prefixes;
        Model specModel = specRoot.getModel();
        PrefixMapping pm = specModel;
        context = new Context();
        Set<String> seen = new HashSet<String>();
    //
        Model rp = getAnyReservedProperties( specRoot );
		context.loadVocabularyAnnotations( seen, rp );
        context.loadVocabularyAnnotations( seen, specModel );
    //
        extractDatatypes( specModel );
    //
        for (NodeIterator i = specModel.listObjectsOfProperty(specRoot, API.vocabulary); i.hasNext();) {
        	String vocabLoc = getLexicalForm(i.next());
            Model vocabModel = loader.loadModel( vocabLoc );
			loadVocabulary( vocabModel, seen, prefixes, pm );
        }
    //
        for (Model vocab: BuiltIn.vocabularies) {
        	loadVocabulary( vocab, seen, prefixes, pm );
        }
    //
        context.checkShortnames();
    }

	public void loadVocabulary(Model vocab, Set<String> seen, PrefixMapping prefixes, PrefixMapping pm) {
		extractDatatypes( vocab );
		context.loadVocabularyAnnotations( seen, vocab, prefixes);
	}

    /**
        Answer a model containing the reserved properties (result, etc) as
        shortname declarations in a model, or an empty model if the legacy
        compatability property is set on the spec.
    */
	private Model getAnyReservedProperties( Resource root ) {
		return root.hasLiteral( EXTRAS.allowSyntaxProperties, true ) ? emptyModel : Reserved.reservedProperties;
	}
    
    private void extractDatatypes( Model m ) {
	    List<Resource> dataTypes = m.listStatements( null, RDF.type, RDFS.Datatype ).mapWith( Statement.Util.getSubject ).toList();
		for (Resource t: dataTypes) declareDatatype( t.getURI() );
		for (Resource p: m.listSubjectsWithProperty( RDF.type, OWL.DatatypeProperty ).toList()) {
			for (RDFNode t: m.listObjectsOfProperty( p, RDFS.range ).toList()) {
				declareDatatype( t.asResource().getURI() );
			}
		}
    }
    
    protected static final Model emptyModel = ModelFactory.createDefaultModel();
    
    protected static final Resource rootResource = emptyModel.createResource( "elda:root/" );
    
    public StandardShortnameService() {
    	this( rootResource, RDFUtils.noPrefixes, null );
    }
    
    public StandardShortnameService( Model config ) {
    	this( config.createResource( "elda:root" ), config, null );
    }
    
    /**
     * Get the prefix/URI mapping defined for this API
     */
    public PrefixMapping getPrefixes() {
        return prefixes;
    }
    
    /**
        Find the full URI for the given short name, or null if we can't
        find one.
    */
    @Override public String expand( String shortName ) {
    	String uri = context.getURIfromName(shortName);
    	return uri == null ? Transcoding.decode( prefixes, shortName ) : uri;
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
        throw new UnknownShortnameException( res );
    }
	
    @Override public Resource asResource( String res ) {
        String uri = expand( res );
        if (uri == null &&  RDFUtil.looksLikeURI(res)) {
            uri = res;
        }
        if (uri != null)
            return ResourceFactory.createResource(uri);
        throw new UnknownShortnameException( res );
    }
    
    /**
     * Return a Context object suitable for driving the JSON encoding
     */
    @Override public Context asContext() {
        return context;
    }
    
    protected final Set<String> datatypes = new HashSet<String>();
    
    public void declareDatatype( String type ) {
    	datatypes.add( type );
    }
    
    protected final static String rdf_XMLLiteral = RDF.getURI() + "XMLLiteral";
    
    @Override public boolean isDatatype( String type ) {
    	return 
    		datatypes.contains( type )
    		|| type.startsWith( XSD.getURI() )
    		|| type.equals( rdf_XMLLiteral )
    		;
	}

	@Override public Map<String, String> constructURItoShortnameMap(Model m, PrefixMapping pm) {
		return new CompleteContext(CompleteContext.Mode.RoundTrip, context, pm).Do(m,  pm);
	}

	public Map<String, String> copyWithout(Map<String, String> baseMap, Map<String, String> toRemove) {
		Map<String, String> contextWithoutName = new HashMap<String, String>( baseMap );
		contextWithoutName.entrySet().removeAll( toRemove.entrySet() );
		return contextWithoutName;
	}

	@Override public ContextPropertyInfo getPropertyByName(String shortName) {
		return context.getPropertyByName( shortName );
	}
}

