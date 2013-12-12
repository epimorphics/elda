/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
    
    File:        EncoderPlugin.java
    Created by:  Dave Reynolds
    Created on:  21 Dec 2009
*/

package com.epimorphics.jsonrdf;

import org.apache.jena.atlas.json.*;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Signature for plugins which perform all the encoding decisions.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface EncoderPlugin {
    /** String used as the property name for identifying resources */
    public String getPNResourceID();  
    
    /** Encode a resource URI */
    public String encodeResourceURI(String uri);
    
    /** Encode a resource URI, use relative URIs if possible, use shortnames only if flag is set */
    public String encodeResourceURI(String uri, ReadContext context, boolean shortNames);
    
    /** Encode a reference to a bNode via a mapped identifier number */
    public String encodebNodeId(int id);
    
    /** 
     	Encode a literal as a JSON compatible object. If isStructured is true,
     	then encode it as an object with _value, _lang, and/or _datatype
     	properties. Use c to shorten type names.
    */
    public void encodeLiteral( JSONWriterFacade jw, boolean isStructured, Literal lit, ReadContext c );
    
    /** Write the outer result wrapper.  */
    public void writeHeader(JSONWriterFacade jw);
    
    /** Write header for a results/model array object   */
    public void startResults(JSONWriterFacade jw, boolean oneResult ) ;

    /** end of results */
	public void endResults( JSONWriterFacade jw, boolean oneResult );
    
    /** Start a sub-section for outputing named graphs */
    public void startNamedGraphs(JSONWriterFacade jw);
    
    /** Start a specific named graph */
    public void startNamedGraph(JSONWriterFacade jw, String name) ;
    
    /** Finish a specific named graph */
    public void finishNamedGraph(JSONWriterFacade jw);
    
    /** Finish the entire second of named graphs, assumes last graph has been closed */
    public void finishNamedGraphs(JSONWriterFacade jw);
    
    /** Return the array of encoded graphs from a top level JSON results set, or null if there is none */
    public JsonArray getNamedGraphs(JsonObject jobj)  throws JsonException;
    
    /** Return the name of a named graph */
    public String getGraphName(JsonObject graph, Context context) throws JsonException ;
    
    
    /** Extract the results part of a deserialized JSON object */
    public JsonArray getResults(JsonObject jobj) throws JsonException;
    
    /** Decode a resource URI */
    public String decodeResourceURI(String code, Context context);
    
    /** Decode an RDF value (object of a statement) */
    public RDFNode decodeValue(Object jsonValue, Decoder decoder, String type);
}

