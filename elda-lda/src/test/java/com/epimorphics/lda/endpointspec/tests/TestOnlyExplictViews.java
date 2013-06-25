package com.epimorphics.lda.endpointspec.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestOnlyExplictViews {
	
	Model spec = ModelIOUtils.modelFromTurtle
		( ":viewer a api:Viewer; api:name 'explicit'; api:property :example."
		+ "\n:s a api:API; api:endpoint :e; api:sparqlEndpoint <http://example.com/none>."
		+ "\n:e a api:ListEndpoint; api:uriTemplate '/absent/friends'"
		+ "\n; api:viewer :viewer."
		);

	Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
	Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
		
	@Test public void testEndpointImplUsedWhere()
		{
		APISpec a = SpecUtil.specFrom( s );
		APIEndpointSpec eps = new APIEndpointSpec( a, a, e );
		assertEquals( CollectionUtils.set("explicit"), eps.getExplicitViewNames() );
		}
}
