/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query;

import java.util.Set;

import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RenderExpression;
import com.epimorphics.lda.rdfq.Variable;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Arguments to query set by handling of query parameters.
    
 	@author chris
*/
public interface QueryArguments {

	public void addSubjectHasProperty( Resource r, Variable v );

	public void addNumericRangeFilter( Variable v, double x, double dx );

	public Variable newVar();

	public void setPageSize( int size );

	public void setPageNumber(int number );

	public void addMetadataOptions( String[] options );

	public void addSearchTriple( String val );

	public void setFixedSelect( String select );

	public void setDefaultLanguage( String defaults );

	public void addWhere( String where );

	public void setSubject( String subject );

	public void setViewByTemplateClause( String clause );

	public void setSortBy( String term );

	public void setOrderBy(String val);

	public void addPropertyHasValue( Param param, Set<String> allVal );

	public void addNameProp( Param plain, String val );

	public void setLanguagesFor( String fullParamName, String languages );

	public void clearLanguages();

	public void addPropertyHasValue( Param param );

	public void addPropertyHasntValue(Param param);

	public void addFilterExpression( RenderExpression exp );

	public void addInfixSparqlFilter( Any l, String op,	Any r );

	public boolean isBindable(String pString);

	public String getDefaultLanguage();
}
