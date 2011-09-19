/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.RenderExpression;
import com.epimorphics.lda.rdfq.Variable;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Wrapper for APIQuery to satisfy the "create query arguments from
    URL query parameters" inferface. Will be moving other functionality 
    in here.
    
 	@author chris
*/
public class QueryArgumentsImpl implements QueryArguments {
	final APIQuery query;
	
	public QueryArgumentsImpl(APIQuery query) {
		this.query = query;
	}

	public void updateQuery() {
		query.addTriplePatterns( triplePatterns );
		query.filterExpressions.addAll( filterExpressions );
	}
	
	protected final List<RDFQ.Triple> triplePatterns = new ArrayList<RDFQ.Triple>();
	protected final List<RenderExpression> filterExpressions = new ArrayList<RenderExpression>();

	@Override public void addFilterExpression( RenderExpression exp ) {
		filterExpressions.add( exp );			
	}
	
	@Override public void addSubjectHasProperty( Resource r, Variable v ) {
		query.addSubjectHasProperty( r, v );
	}

	@Override public void addNumericRangeFilter( Variable v, double x, double dx ) {
		addInfixSparqlFilter( RDFQ.literal( x - dx ), "<", v );
		addInfixSparqlFilter( v, "<", RDFQ.literal( x + dx) );
	}

    @Override public void addInfixSparqlFilter( Any l, String op, Any r ) {
    	addFilterExpression( RDFQ.infix( l, op, r ) );
    }
    
	@Override public Variable newVar() {
		return query.newVar();
	}

	@Override public void setPageSize( int size ) {
		query.setPageSize( size );
	}

	@Override public void setPageNumber(int number) {
		query.setPageNumber( number );
	}

	@Override public void addMetadataOptions(String[] options) {
		query.addMetadataOptions( options );
	}

	@Override public void addSearchTriple( String term ) {
		query.addSearchTriple( term );
	}

	@Override public void setFixedSelect(String select) {
    	query.fixedSelect = select;
	}

	@Override public void setDefaultLanguage( String defaults ) {
		query.setDefaultLanguage( defaults );
	}

	@Override public void addWhere( String where ) {
		query.addWhere( where );			
	}

	@Override public void setSubject( String subject ) {
		query.setSubject( subject );		
	}

	@Override public void setViewByTemplateClause( String clause ) {
		query.setViewByTemplateClause( clause );			
	}

	@Override public void setSortBy(String term) {
		query.setSortBy( term );
	}

	@Override public void setOrderBy(String term) {
		query.setOrderBy( term );
	}

	@Override public void addPropertyHasValue(Param param, Set<String> allVal) {
		query.addPropertyHasValue( param, allVal );
	}

	@Override public void addNameProp(Param plain, String val) {
		query.addNameProp( plain, val );
	}

	@Override public void setLanguagesFor( String fullParamName, String languages ) {
		query.setLanguagesFor( fullParamName, languages );
	}

	@Override public void clearLanguages() {
		query.clearLanguages();
	}

	@Override public void addPropertyHasValue(Param param) {
		query.addPropertyHasValue( param );			
	}

	@Override public void addPropertyHasntValue( Param param ) {
		query.addPropertyHasntValue( param );
	}

	@Override public String getDefaultLanguage() {
		return query.defaultLanguage;
	}
}