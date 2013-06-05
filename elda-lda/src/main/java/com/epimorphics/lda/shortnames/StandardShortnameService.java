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

import java.util.*;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.UnknownShortnameException;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.*;

import com.hp.hpl.jena.rdf.model.impl.Util;

/**
	See ShortnameService.
	
 	@author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class StandardShortnameService implements ShortnameService {

    protected final Context context;
    protected final PrefixMapping prefixes;
    protected final NameMap nameMap = new NameMap();
    
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
        nameMap.load( pm, rp );
        nameMap.load( pm, specModel );
    //
        for (NodeIterator i = specModel.listObjectsOfProperty(specRoot, API.vocabulary); i.hasNext();) {
        	String vocabLoc = getLexicalForm(i.next());
            Model vocabModel = loader.loadModel( vocabLoc );
			loadVocabulary( vocabModel, seen, prefixes, pm );
            nameMap.load( pm, vocabModel );
        }
    //
        for (Model vocab: BuiltIn.vocabularies) {
        	loadVocabulary( vocab, seen, prefixes, pm );
        	nameMap.load( pm, vocab );
        }
    //
        nameMap.done();
    }

	public void loadVocabulary(Model vocab, Set<String> seen, PrefixMapping prefixes, PrefixMapping pm) {
		extractDatatypes( vocab );
		nameMap.loadIfNotDefined( pm, vocab );
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
		
		if (true) return new CompleteContext(CompleteContext.Mode.Transcode, context, pm).Do(m,  pm);
		
		String it = "http://purl.org/linked-data/api/vocab#processor";	
				
		Map<String, String> byContext = contextToNameMap(m, pm);
		Map<String, String> byNameMap = nameMap.stage2().loadPredicates(pm, m).constructURItoShortnameMap();
		if (true || byContext.equals(byNameMap)) {} else {
			System.err.println( "\n>> MISMATCHED URI->SHORTNAME MAP:" );
			
			String A = byContext.get( it );
			String B = byNameMap.get( it );
			System.err.println( ">> " + A + " vs " + B );			
			
			Map<String, String> contextWithoutName = copyWithout(byContext, byNameMap);

			Map<String, String> nameWithoutContext = copyWithout(byNameMap, byContext);
			
			System.err.println( ">> elements only in context: " );
			for (Map.Entry<String, String> e: contextWithoutName.entrySet()) 
				System.err.println( ">>  " + e );
			
			System.err.println( ">> elements only in namemap: " );
			for (Map.Entry<String, String> e: nameWithoutContext.entrySet()) 
				System.err.println( ">>  " + e );		
			
			 throw new RuntimeException("BOOM");
		}
		return byNameMap;
	}

	public Map<String, String> copyWithout(Map<String, String> baseMap, Map<String, String> toRemove) {
		Map<String, String> contextWithoutName = new HashMap<String, String>( baseMap );
		contextWithoutName.entrySet().removeAll( toRemove.entrySet() );
		return contextWithoutName;
	}

	private Map<String, String> contextToNameMap(Model m, PrefixMapping pm) {
		
		// System.err.println( "\n\n>> (((((((((((((((((((((((((((((((((((((((((((((((((((" );
		
		Map<String, String> result = new HashMap<String, String>();
	//
		result.put(API.value.getURI(), "value");
		result.put(API.label.getURI(), "label");
	//
		for (String key: context.preferredNames()) {
			String uri = context.getURIfromName( key );
			
			if (uri.equals(DOAP.programming_language.getURI())) {
				result.put(uri, "programming_language" );
			} else if (result.containsKey( uri )) {
				String otherKey = result.get(uri);
//				 System.err.println( ">> CLASH for " + uri + ", might be " + key + ", might be " + otherKey );
				if (otherKey.length() > key.length()) { String x = key; key = otherKey; otherKey = x; }
				if (key.endsWith(otherKey)) 
					result.put(uri, otherKey);
				else
					if (false) System.err.println( ">> -- could not resolve clash for " + uri + " -- " + key + " vs " + otherKey + "." );
			} else {
				result.put( uri, key );
			}
//			if (result.containsKey( uri )) {
//				String XXX = result.get( uri );
//				System.err.println( "-->> NOTE, " + uri + " already has short name " + XXX + " but is being given name " + key );
//			}
		}
		
		// System.err.println( ">> )))))))))))))))))))))))))))))))))))))))))))))))))))))" );
		
		String it = "http://purl.org/linked-data/api/vocab#processor";
//		for (String name: context.allNames()) System.err.println( ">>    " + name );
		
		Set<String> modelTerms = new HashSet<String>();
		
		for (StmtIterator sit = m.listStatements(); sit.hasNext();) {
			Statement s = sit.next();
			modelTerms.add( s.getPredicate().getURI() );
			Node o = s.getObject().asNode();
			if (o.isLiteral()) {
				String type = o.getLiteralDatatypeURI();
				if (type != null) modelTerms.add( type );
			}
		}
		
		// loadMagic( mapURItoShortName );
		
		modelTerms.removeAll( result.keySet() );
		for (String mt: modelTerms) {
			int cut = com.hp.hpl.jena.rdf.model.impl.Util.splitNamespace( mt );
			String namespace = mt.substring( 0, cut );
			String shortName = mt.substring( cut );
			if (namespace.equals( "eh:/" )) {
				// TODO testing hack: fix and remove.
				result.put( mt, shortName );
			} else {						
				result.put( mt, Transcoding.encode( prefixes, mt ) );
			}
		}
		
		return result;
	}

	@Override public ContextPropertyInfo getPropertyByName(String shortName) {
		ContextPropertyInfo byContext = context.getPropertyByName( shortName );
		ContextPropertyInfo byNameMap =  nameMap.getPropertyByName(shortName);
		if (false && byContext != byNameMap) {
			System.err.println( ">> MISMATCHED CONTEXT PROPERTY INFO for: " + shortName );
			if (byContext.equals(byNameMap)) System.err.println( ">>  [but they are .equals()]" );
			System.err.println( ">>  byContext: " + byContext );
			System.err.println( ">>  byNameMap: " + byNameMap );
//			throw new RuntimeException("BOOM");
		}			
		return byNameMap;
	}
}

