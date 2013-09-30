package com.epimorphics.lda.textsearch;

import java.util.List;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.AnyList;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TextSearchConfig {

    public final static Property JENA_TEXT_QUERY = ResourceFactory.createProperty( "http://jena.apache.org/text#query" );
    
    public final static Property DEFAULT_CONTENT_PROPERTY = RDFS.label;
    
	final Property textQueryProperty;
	final Property textContentProperty;
	final AnyList textSearchOperand;
	final boolean placeEarly;

	/**
	    Configure this TextSearchConfig according to the properties of the
	    given endpoint, which may be any of a SPARQL endpoint resource,
	    an APISpec resource, or an APIEndpoint resource.
	*/
	public TextSearchConfig( Resource endpoint ) {
		this
			( configTextQueryProperty( endpoint, JENA_TEXT_QUERY )
			, configTextContentProperty( endpoint, DEFAULT_CONTENT_PROPERTY )
			, configTextSearchOperand( endpoint, null )
			, configPlaceEarly( endpoint, true )
			);
	}
	
	public TextSearchConfig() {
		this( JENA_TEXT_QUERY, DEFAULT_CONTENT_PROPERTY, null, true );
	}
	
	/**
	    Answer a new TextSearchConfig whose content this this ones overlaid
	    with configurations from the given endpoint.
	    // NOTE the "overlaying" isn't done yet.
	*/
	public TextSearchConfig overlay( Resource endpoint ) {
		return new TextSearchConfig
			( configTextQueryProperty( endpoint, textQueryProperty )
			, configTextContentProperty( endpoint, textContentProperty )
			, configTextSearchOperand( endpoint, textSearchOperand )
			, configPlaceEarly( endpoint, placeEarly )
			);
	}
	
	private static boolean configPlaceEarly(Resource endpoint, boolean placeEarly) {
		Statement b = endpoint.getProperty( EXTRAS.textPlaceEarly );
		return b == null ? placeEarly : b.getBoolean();
	}

	private TextSearchConfig
		( Property textQueryProperty
		, Property textContentProperty
		, AnyList textSearchOperand
		, boolean placeEarly
		) {
		this.textQueryProperty = textQueryProperty;
		this.textContentProperty = textContentProperty;
		this.textSearchOperand = textSearchOperand;
		this.placeEarly = placeEarly;
	}
	
	private static Property configTextQueryProperty( Resource endpoint, Property ifUnspecified ) {
		Resource tqp = endpoint.getPropertyResourceValue( EXTRAS.textQueryProperty );
		return tqp == null ? ifUnspecified : tqp.as(Property.class);
	}
	
	private static Property configTextContentProperty( Resource endpoint, Property ifUnspecified ) {
		Resource tcp = endpoint.getPropertyResourceValue( EXTRAS.textContentProperty );
		return tcp == null ? ifUnspecified : tcp.as(Property.class);
	}

	private static AnyList configTextSearchOperand(Resource endpoint, AnyList ifUnspecified) {
		Resource tso = endpoint.getPropertyResourceValue( EXTRAS.textSearchOperand );
		return tso == null ? ifUnspecified : convertList(tso);
	}
	
	private static AnyList convertList(Resource tso) {
		if (tso.canAs(RDFList.class)) {
			List<RDFNode> operand = tso.as(RDFList.class).asJavaList();
			Any[] elements = new Any[operand.size()];
			for (int i = 0; i < operand.size(); i += 1) elements[i] = RDFQ.any( operand.get(i) );
			return RDFQ.list( elements );
		} else {
			EldaException.BadSpecification( "Object " + tso + " of " + EXTRAS.textSearchOperand + " must be an RDF list." );
			return /* never */ null;
		}
	}

    /**
        Return the configured text query property, which defaults to
        JENA_TEXT_SEARCH.
    */
    public Property getTextQueryProperty() {
    	return textQueryProperty;
    }

    /**
        Return the configured text content property, which defaults to
        rdfs:label.
    */
	public Property getTextContentProperty() {
		return textContentProperty;
	}

	/**
	    Return the configured text search operand, or null if no operand
	    was configured.
	*/
	public AnyList getTextSearchOperand() {
		return textSearchOperand;
	}
	
	/**
	 	Returns true if text search properties should be ordered early,
	 	false if they should be ordered late.
	*/
	public boolean placeEarly() {
		return placeEarly;
	}

}
