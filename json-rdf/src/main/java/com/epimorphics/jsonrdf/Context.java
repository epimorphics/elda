/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        Context.java
    Created by:  Dave Reynolds
    Created on:  21 Dec 2009
*/

package com.epimorphics.jsonrdf;

import static com.epimorphics.jsonrdf.RDFUtil.*;

import java.util.*;
import java.util.regex.Pattern;

import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Encapsulates the mapping decisions to determine how RDF should be
 * serialized in JSON to enable partial inversion. The source context
 * information may be manually or automatically generated.
 * The context itself should serializable in JSON to enable (partial)
 * round tripping.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class Context implements ReadContext, Cloneable {

    protected String base = null;
    
    protected Map<String, ContextPropertyInfo> uriToProp = new HashMap<String, ContextPropertyInfo>();
    
    protected Map<String, String> uriToName = new HashMap<String, String>();
    
    protected Map<String, String> nameToURI = new HashMap<String, String>();
    
    protected int nameCount = 0;
    
    protected boolean sortProperties = false;
    
    protected boolean completedMappingTable = false;
    
    @Override public String toString() {
    	return "context:"
    		+ "\nuriToProp: " + uriToProp
    		+ "\nuriToName: " + uriToName
    		+ "\nnameToURI: " + nameToURI
    		+ "\n"
    		;
    }
    
//    @Override public boolean equals(Object other) {
//    	return other instanceof Context && same( (Context) other );
//    }
//    
//    protected boolean same(Context other) {
//    	return
//    		uriToProp.equals(other.uriToProp)
//    		&& uriToName.equals(other.uriToName)
//    		&& nameToURI.equals(other.nameToURI)
//    		&& sortProperties == other.sortProperties
//    		&& completedMappingTable == other.completedMappingTable
//    		;
//    }
//    
//    public String diff( Context other ) {
//    	StringBuilder sb = new StringBuilder();
//    	if (!uriToProp.equals(other.uriToProp)) diff( sb, "uriToProp", uriToProp, other.uriToProp );
//    	if (!uriToName.equals(other.uriToName)) diff( sb, "uriToName", uriToName, other.uriToName );
//    	if (!nameToURI.equals(other.nameToURI)) diff( sb, "nameToURI", nameToURI, other.nameToURI );
//    	if (sortProperties != other.sortProperties) sb.append( "sortproperties: " ).append(other.sortProperties);
//    	if (completedMappingTable != other.completedMappingTable) sb.append( "completedMappingTable: " ).append(other.completedMappingTable);
//    	return sb.toString();
//    }
//    
//    
//    private static <K, V> void diff( StringBuilder sb, String name, Map<K, V> a, Map<K, V> b) {
//		if (!a.equals(b)) {
//			sb.append( "\n" ).append( name );
//			Set<K> aKeys = a.keySet(), bKeys = b.keySet();
//			if (!aKeys.equals(bKeys)) {
//				Set<K> aKeysOnly = new HashSet<K>( aKeys ); aKeysOnly.removeAll( bKeys );
//				Set<K> bKeysOnly = new HashSet<K>( bKeys ); bKeysOnly.removeAll( aKeys );
//				Set<K> sharedKeys = new HashSet<K>( aKeys ); sharedKeys.retainAll( bKeys );
//				sb.append( "\n  shared: " ).append( sharedKeys );
//				sb.append( "\n  aKeys: " ).append( aKeysOnly );
//				sb.append( "\n  bKeys: " ).append( bKeysOnly );
//			}
//		}
//	}

	/**
     * Construct an empty context
     */
    public Context() {
    }
    
    /**
     * Construct a context, initialized from an ontology.
     * @param ontology ontology model used for naming, and annotation to control serializations
     */
    public Context(Model ontology) {
        loadVocabularyAnnotations( new HashSet<String>(), ontology);
    }
    
    /**
     * Construct a context with a defined base URI
     * @param base URI used for relative referencing
     */
    public Context(String base) {
        this.base = base;
    }
    
    /**
        Clone this context, so that JSON rendering using the clone does
        not affect this context. Each Prop object must also be cloned.
    */
    @Override public Context clone() {
    	try {
    		Context result = (Context) super.clone();
    		result.uriToName = new HashMap<String, String>( uriToName );
    		result.uriToProp = new HashMap<String, ContextPropertyInfo>();
    		for (Map.Entry<String, ContextPropertyInfo> e: uriToProp.entrySet()) {
    			result.uriToProp.put( e.getKey(), e.getValue().clone() );
    		}
    		result.nameToURI = new HashMap<String, String>( nameToURI );
    		return result;
    	} catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't happen :)", e);
    	}
    }

    
    public void loadVocabularyAnnotations(Set<String> seen, Model m) {
        loadVocabularyAnnotations(seen, m, m);
    }
    
    protected final PrefixMapping allPrefixes = PrefixMapping.Factory.create();
    /**
     	Scan the given vocabulary file to find shortname and property type
     	annotations. Ignore any URIs in <code>notThese</code>. Update
     	notThese with any new URIs once we're done.
    */
    public void loadVocabularyAnnotations( Set<String> notThese, Model m, PrefixMapping prefixes) {  
    	allPrefixes.withDefaultMappings( prefixes );
    	Set<String> seen = new HashSet<String>();
        for(Resource r : RES_TYPES_TO_SHORTEN) 
            loadAnnotations(notThese, seen, m, m.listSubjectsWithProperty(RDF.type, r), false, prefixes);
        for(Resource r : PROP_TYPES_TO_SHORTEN) 
            loadAnnotations(notThese, seen, m, m.listSubjectsWithProperty(RDF.type, r), true, prefixes);
        loadAnnotations(notThese, seen, m, m.listSubjectsWithProperty(API.label), false, prefixes);
        loadAnnotations(notThese, seen, m, m.listSubjectsWithProperty(RDFS.range), true, prefixes);
        notThese.addAll( seen );
    }
    
    public static Resource[] RES_TYPES_TO_SHORTEN = new Resource[] {RDFS.Class, OWL.Class};
    
    public static Resource[] PROP_TYPES_TO_SHORTEN = new Resource[] {RDF.Property, OWL.DatatypeProperty, OWL.ObjectProperty, API.Hidden};
    
    public static Pattern labelPattern = Pattern.compile("[_a-zA-Z][0-9a-zA-Z_]*");
    
    protected void loadAnnotations( Set<String> notThese, Set<String> seen, Model m, ResIterator ri, boolean isProperty, PrefixMapping prefixes) {
        while (ri.hasNext()) {
            Resource res = ri.next();
            String uri = res.getURI();
            if (uri != null) {
            	String shortForm = null;
            	seen.add( uri );
            	if (!notThese.contains(uri)) {
            		shortForm = setShortForms( prefixes, res, uri );
            	}
            	if (isProperty) {
        		    if (shortForm == null) shortForm = getLocalName(uri);
        		    createPropertyRecord( shortForm, res );
            	}
            }
        }
    }

	private String setShortForms( PrefixMapping prefixes, Resource res, String uri) {
		String shortForm = null;
		recordAltName(uri, prefixes);
		if (res.hasProperty(API.label)) {
		    shortForm = getStringValue(res, API.label);
		    recordPreferredName(shortForm, uri);
		} else if (res.hasProperty(RDFS.label)) {
		    shortForm = getStringValue(res, RDFS.label);
		    if (labelPattern.matcher(shortForm).matches()) {
		        recordPreferredName(shortForm, uri);
		    }
		}
		return shortForm;
	}
    
    /**
     * Record the preferred name to use to shorten a URI.
     * If the name is already in use then only record as an alternate name
     */
    public void recordPreferredName(String name, String uri) {
        if (isNameFree(name)) { 
            nameToURI.put(name, uri);
            uriToName.put(uri, name);
            ContextPropertyInfo prop = uriToProp.get(uri);
            if (prop != null && !prop.getName().equals(name)) {
                prop.setName(name);
            }
        } 
    }
    
    static final Literal Literal_TRUE = ResourceFactory.createTypedLiteral( true );
    
    protected void createPropertyRecord( String name, Resource res ) {
        String uri = res.getURI();
        ContextPropertyInfo prop = uriToProp.get(uri);
        if (prop == null) {
            prop = new ContextPropertyInfo(uri, name);
            uriToProp.put(uri, prop);
        }
        if (res.hasProperty( RDF.type, API.Multivalued)) prop.setMultivalued(true);
        if (res.hasProperty( API.multiValued )) prop.setMultivalued( res.getProperty( API.multiValued ).getBoolean() );
        if (res.hasProperty( API.structured ) ) prop.setStructured( res.getProperty( API.structured ).getBoolean() );
        if (res.hasProperty( RDF.type, API.Hidden)) prop.setHidden(true);
        if (res.hasProperty( RDF.type, OWL.ObjectProperty )) prop.setType(OWL.Thing.getURI());
        if (res.hasProperty( RDFS.range ) && prop.getType() == null) prop.setType( getStringValue(res, RDFS.range) );
    }
    
    /**
     * Record an alternative named to use to to shorted a URI.
     * Will only be used when expanding queries, not for generation of shortform listings
     */
    protected void recordAltName(String name, String uri) {
        if ( ! nameToURI.containsKey(name)) nameToURI.put(name, uri);
        // Only the preferred name goes in the uriToName mapping
    }
    
    protected void recordAltName(String uri, PrefixMapping pm) {
        // Note local name
        recordAltName( getLocalName(uri), uri );
        // Note prefixed name
        String sf = pm.shortForm(uri);
        if (!sf.equals( uri )) recordAltName( sf.replace(':', '_'), uri );
    }

    protected String getLocalName(String uri) {
        return uri.substring( Util.splitNamespace( uri ));
    }

    /**
     * Finish off the mapping table filling in anything we have useful prefixes for
     * and inventing non-clashing forms.
     */
    protected void completeContext() {
        if ( !completedMappingTable ) {
            completedMappingTable = true;
            for (Map.Entry<String, String> e : nameToURI.entrySet()) {
                String uri = e.getValue();
                String name = e.getKey();
                if ( ! uriToName.containsKey(uri)) {
                    uriToName.put(uri, name);
                }
            }
        }
    }
    
    /** Return the base URI assumed during serialization */
    public String getBase() {
        return base;
    }

    /** Set the base URI */
    public void setBase(String base) {
        this.base = base;
    }
    
    /** Set flag to indicate if properties should be sorted in the encoding */
    public void setSorted(boolean sorted) {
        this.sortProperties = sorted;
    }
    
    public boolean isSortProperties() {
        return sortProperties;
    }
    
    /** The set of all mapped names */
    public Set<String> allNames() {
        return nameToURI.keySet();
    }
    
    /** Lookup the definition of a property based on its URI */
    public ContextPropertyInfo getPropertyByURI(String uri) {
        return uriToProp.get(uri);
    }
    
    /** Lookup the definition of a property based on its mapped name */
    public ContextPropertyInfo getPropertyByName(String name) {
        return getPropertyByURI( getURIfromName(name) );
    }
    
    /** Lookup the shortened form for a URI, can apply to non-properties (e.g. classes) as well as properties */
    public String getNameForURI(String uri) {
        completeContext();
        return uriToName.get(uri);
    }
    
    public String forceShorten( String uri ) {
    	String shorter = allPrefixes.shortForm( uri );
    	return shorter.equals( uri ) ? null : shorter.replace( ':', '_' );
    }
    
    /** Lookup the URI for a shortened name. Returns null if no mapping is known */
    public String getURIfromName(String name) {
        completeContext();
        return nameToURI.get(name);
    }
    
    /**
     * Determine an appropriate name for a property resource, creating a new
     * context entry if required. 
     */
    public String findNameForProperty(Resource r) {
        String uri = r.getURI();
        String name = getNameForURI( uri );
        
        if (name == null) {         
        	// Try just using localname
            String localname = r.getLocalName(); 
            if ( nameUpdateOK(localname, uri) ) return localname; 
            // See if we can generate a prefix form
            name = r.getModel().shortForm(uri);
            if (! name.equals(uri)) {
                name = name.replace(':', '_');
                if ( nameUpdateOK(name, uri) ) return name;
            }
            
            // Start making ones up as a last resort
            while (true) {
                name = localname + nameCount++;
                if ( nameUpdateOK(name, uri) ) return name;
            }
        } else {
            return name;
        }
    }

    protected boolean nameUpdateOK(String name, String uri) {
        if (isNameFree(name)) {
            recordPreferredName(name, uri);
            return true;
        }
        return false;
    }

    /**
     * Determine record for a property, creating a new
     * context entry if required.
     */
    public ContextPropertyInfo findProperty(Property p) {
        String uri = p.getURI();
        ContextPropertyInfo prop = getPropertyByURI(uri);
        if (prop == null) {
            String name = findNameForProperty(p);
            prop = getPropertyByURI(uri);
            if (prop == null) {
                prop = new ContextPropertyInfo(uri, name);
                uriToProp.put(uri, prop);
            }
        }
        return prop;
    }
    
    /** Test if a name is not already in use */
    protected boolean isNameFree( String name ) {
        String uri = nameToURI.get(name);
        if (uri == null) {
            // No entry at all so it is definitely free
            return true;
        } else {
            // Might be a non-preferred entry
            String prefname = uriToName.get(uri);
            return (prefname == null) || (! prefname.equals(name));
        }
    }
    
    public void setProperty(String uri, ContextPropertyInfo prop) {
        uriToProp.put(uri, prop);
        String name = prop.getName();
        recordPreferredName(name, uri);  // False to isProperty because we are registering an externally created prop ourselves
    }
    
}

