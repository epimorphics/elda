/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
    
    File:        RDFUtil.java
    Created by:  Dave Reynolds
    Created on:  27 Dec 2009
*/

package com.epimorphics.jsonrdf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class RDFUtil {

	public static final String RDFPlainLiteral = RDF.getURI() + "PlainLiteral";
	
	public static class Vocab {

		public static final Resource missingListElement = r( "missingListElement" );
		
		public static final Resource missingListTail = r( "missingListTail" );
		
		public static final String NS = "http://www.epimorphics.com/vocabularies/lda#";
		
		public static Resource r( String localName ) {
			return ResourceFactory.createResource( NS + localName );
		}
	}

    /**
     * Return one of the values of the property on the resource in string form.
     * If there are no values return the defaultValue. If the value is not
     * a String but is a literal return its lexical form. If it is a resource
     * return its URI. 
     */
    public static String getStringValue(Resource r, Property p, String defaultValue) {
        Statement s = r.getProperty(p);
        if (s == null) {
            return defaultValue;
        } else {
            return getLexicalForm( s.getObject() );
        }
    }

    /**
     * Return one of the values of the property on the resource in string form.
     * If there are no values return null.
     */
    public static String getStringValue(Resource r, Property p) {
        return getStringValue(r, p, null);
    }
    
    /**
     * Return the lexical form of a node. This is the lexical form of a
     * literal, the URI of a URIResource or the annonID of a bNode.
     */
    public static String getLexicalForm(RDFNode value) {
        if (value.isLiteral()) {
            return ((Literal)value).getLexicalForm();
        } else if (value.isURIResource()) {
            return ((Resource)value).getURI();
        } else {
            return value.toString();
        }
    }
    
	/**
    	Answer the Java list rooted at <code>l</code>, but if the list is
    	incomplete, just deliver the existing elements.
	 */
    public static List<RDFNode> asJavaList( Resource l ) {
		List<RDFNode> result = new ArrayList<RDFNode>();
		Model m = l.getModel();
		while (!l.equals( RDF.nil )) {
			Statement first = l.getProperty( RDF.first );
			Statement rest = l.getProperty( RDF.rest );
			if (first == null) {
				result.add(Vocab.missingListElement.inModel( m ) );
			} else {
				result.add( first.getObject() );				
			}
			if (rest == null) {
				result.add( Vocab.missingListTail.inModel( m ) );
				break;
			}
			l = rest.getResource();
		}
		return result;
	}

    /**
        Answer true if <code>v</code> is a list, which here is defined to
        be "a resource which is nil or has an rdf:first or is of type rdf:List". 
    */
    public static boolean isList(RDFNode l) {
    	if (l.isLiteral()) return false;    	
        Resource r = (Resource) l;
		return 
			r.equals( RDF.nil )
			|| r.hasProperty( RDF.first )
			|| r.hasProperty( RDF.type, RDF.List ) 
			;   
    }
    
    /**
        xsdDateFormat returns a SimpleDateFormat  in yyyy-MM-dd form.
        Note that synchronisation issues mean that we can't have a single
        shared constant -- it has mutable state.
    */

    protected static SimpleDateFormat xsdDateFormat() {
    	return new SimpleDateFormat( "yyyy-MM-dd" );
    }  

    /**
        xsdDateFormat returns a SimpleDateFormat  in "EEE, d MMM yyyy HH:mm:ss 'GMT'Z" 
        form, with the timezone set to GMT. Note that synchronisation issues 
        mean that we can't have a single shared constant -- it has mutable state.
    */
    protected static SimpleDateFormat dateFormat() {
    	return dateFormat(true, false);
    }    
    
    public static SimpleDateFormat dateFormat(boolean keepZone, boolean dropTime) {
    	boolean keepTime = !dropTime;
    	String timeFormat = " HH:mm:ss", dateFormat = "EEE, dd MMM yyyy";
    	String formatString = dateFormat;
    	if (keepTime) formatString += timeFormat;
    	if (keepZone && keepTime) formatString += " 'GMT'Z";
    	SimpleDateFormat df = new SimpleDateFormat( formatString ); 
    	if (keepZone) df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    	return df;
    }
    
    /**
     * Convert an xsd:datetype or xsd:date to a javascript compatible string.
     * Returns null if not a supported type
     */
    public static String formatDateTime(Literal l) {
    	Object val = getTemporalValue(l);
        if (val instanceof XSDDateTime) {
        	boolean isDate = l.getDatatype().equals(XSDDatatype.XSDdate);
            Date date = ((XSDDateTime)val).asCalendar().getTime();
            return dateFormat(hasTimeZone(l.getLexicalForm()), isDate).format(date);
        } else {
            return null;
        }
    }
    
    /**
        Returns the date/time object derived from the literal l. As a sop to
        literals of type xsd_Date which have an associated Time, if getting
        the value throws a datatype format exception, try again on a literal
        with the same lexical form but type xsd_dateTime.
    */
    private static Object getTemporalValue(Literal l) {
    	try {
    		return l.getValue();
    	} catch (DatatypeFormatException e) {
    		Literal lit = ResourceFactory.createTypedLiteral(l.getLexicalForm(), XSDDatatype.XSDdateTime );
			return lit.getValue();
    	}
	}



	public static final Pattern matchTimeZone = Pattern.compile( "(Z|[^-0-9][-+]\\d\\d(\\d\\d|:\\d\\d)?)$" );
        
    /**
        Answer true iff this lexical form looks like it ends with a time
        zone. Mild trickery is required so that partial dates are not
        interpreted as time zones as per the syntax expressed in the
        <code>matchTimeZone</code> regular expression -- currently a
        Jena XSDDateTime object may have a lexical form which is partial.
    */
    private static boolean hasTimeZone(String lexicalForm) {
		return matchTimeZone.matcher(lexicalForm).find();
	}

	/**
     	Convert an javascript date string to an xsd:datetime or xsd:date.
     	Dancing around to ensure that we can construct zoneless times
     	(going via Date forcibly introduces a timezone, which we eliminate by
     	turning it into an xsd:dateTime lexical form).
     	@throws ParseException 
    */
    public static Literal parseDateTime(String lex, String type) throws ParseException {
        boolean hasTimeZone = hasTimeZone(lex);
        boolean isXSDDate = XSD.date.getURI().equals(type);
		SimpleDateFormat sdf = dateFormat(hasTimeZone, isXSDDate);
		Date date = sdf.parse(lex);
		if (isXSDDate) {
            return ResourceFactory.createTypedLiteral(xsdDateFormat().format(date), XSDDatatype.XSDdate);
        } else {
        	if (hasTimeZone) {
        		Calendar cal  = Calendar.getInstance( TimeZone.getTimeZone("GMT") );
        		cal.setTime(date);
        		XSDDateTime dt = new XSDDateTime(cal);
        		return ResourceFactory.createTypedLiteral( dt );        		
        	} else {
        		SimpleDateFormat f = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss" );
        		String lf = f.format(date);
				Literal l = ResourceFactory.createTypedLiteral( lf, XSDDatatype.XSDdateTime );
				return l;
        	}
        }
    }
    
    /**
        Check whether a string looks like an (absolute) URI. "Looks like" is
        very sketchy: it means "start with a scheme".
    */
    private static final Pattern uriPattern = Pattern.compile( "[A-Za-z][-+.A-Za-z0-9]*:.*" );
    
    public static boolean looksLikeURI(String s) {
        return uriPattern.matcher(s).matches();
    }

}
