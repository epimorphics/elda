package com.epimorphics.lda.apispec.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestTransmitsPurgeSetting {
	
	@Test public void testSetsCorrectPurgeSetting() {
		testSetsCorrectPurgeSetting( false, null, null );
		testSetsCorrectPurgeSetting( true, true, null );
		testSetsCorrectPurgeSetting( false, false, null );
		testSetsCorrectPurgeSetting( true, null, true );
		testSetsCorrectPurgeSetting( true, null, true );
		testSetsCorrectPurgeSetting( true, null, true );
		testSetsCorrectPurgeSetting( false, null, false );
		testSetsCorrectPurgeSetting( false, null, false );
		testSetsCorrectPurgeSetting( false, null, false );
	}

	private void testSetsCorrectPurgeSetting(boolean expected, Boolean inSpec, Boolean inEndpoint) {
		Model spec = ModelIOUtils.modelFromTurtle
			( ":s a api:API"
			+ "\n; api:sparqlEndpoint <http://example.com/none>"
			+ "\n; api:endpoint :e"
			+ (inSpec == null ? "" : "\n; elda:purgeFilterValues " + inSpec)
			+ "\n."
			+ "\n\n"
			+ "\n:e a api:ListEndpoint"
			+ (inEndpoint == null ? "" : "\n; elda:purgeFilterValues " + inEndpoint)
			+ "\n; api:uriTemplate 'something'"
			+ "\n."
			);
		Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
		APISpec a = SpecUtil.specFrom( s );
		APIEndpointSpec ep = a.getEndpoints().get(0);
		assertEquals( expected, ep.getPurging());		
	}
	
	

}
