/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.epimorphics.lda.core.Param.Info;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.exceptions.BadRequestException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.vocabularies.API;

/**
    ValTranslator handles the translation of the V in ?P=V
    to a suitable RDFQ node, usually (but not always) some
    kind of literal. It may need to add new expressions to
    the query filter (via the Expressions member), generate new
    variables (via the VarSupply member), and look at the
    type information of properties (via the ShortnameService
    member).
    
 	@author chris
*/
public class ValTranslator {
	
	public interface Filters {
		public void add( RenderExpression e );
	}
	
	protected final ShortnameService sns;
	protected final VarSupply vs;
	protected final Filters expressions;
	
	public ValTranslator(VarSupply vs, Filters expressions, ShortnameService sns) {
		this.vs = vs;
		this.sns = sns;
		this.expressions = expressions;
	}

	public static final String[] JUSTEMPTY = new String[]{""};

	public Any objectForValue( Info inf, String val, String languages ) {
		return objectForValue(inf.typeURI, val, languages);
	}
	
	static final IRIFactory iriFactory = IRIFactory.iriImplementation();

	public Any objectForValue( String type, String val, String languages ) {
		String[] langArray = languages == null ? JUSTEMPTY : languages.split( ",", -1 );
		String expanded = sns.expand(val);
		if (type == null) {
			return languagedLiteral(langArray, val);
		} else if (type.equals( API.SimpleLiteral.getURI())) {
			return RDFQ.literal(val);
		} else if (type.equals( API.PlainLiteral.getURI())) {
			return languagedLiteral( langArray, val );
		} else if (sns.isDatatype(type)) {
			return RDFQ.literal( val, null, type );
		} else {
			String uri = expanded == null ? val : expanded;
			if (false) {
				IRI i = iriFactory.create(uri);
				if (i.hasViolation(true)) {
					throw new BadRequestException("illegal IRI '" + uri + "'");
				}
			}
			return RDFQ.uriRaw( uri );
		}
	}

	private Any languagedLiteral(String[] langArray, String val) {
		if (langArray.length == 1) return RDFQ.literal( val, langArray[0], "" );
		
		Variable o = vs.newVar();
		Apply stringOf = RDFQ.apply( "str", o );
		Infix equals = RDFQ.infix( stringOf, "=", RDFQ.literal( val ) );
		Infix filter = RDFQ.infix( equals, "&&", ValTranslator.someOf( o, langArray ) );
		expressions.add( filter );
		
		return o;
	}

	/**
	 	Generates lang(v) = l1 || lang(v) = l2 ... for each l1... in langArray.
	*/
	static RenderExpression someOf( Any v, String[] langArray ) {
		Apply langOf = RDFQ.apply( "lang", v );
		RenderExpression result = RDFQ.infix( langOf, "=", ValTranslator.omitNone( langArray[0] ) );
		for (int i = 1; i < langArray.length; i += 1)
			result = RDFQ.infix( result, "||", RDFQ.infix( langOf, "=", ValTranslator.omitNone( langArray[i] ) ) );
		return result;
	}

	/**
	     Answers lang unless it is "none", in which case it answers "".
	*/
	static Any omitNone( String lang ) {
		return RDFQ.literal( lang.equals( "none" ) ? "" : lang );
	}

}
