/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.endpointspec.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.apispec.tests.SpecUtil;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Test that api:include works on viewers.
 	@author eh
*/
public class TestIncludeViewers {
	
	@Test public void testViewer() {
		String NS = "http://www.epimorphics.com/tools/example#";
		Model m = ModelIOUtils.modelFromTurtle
			( ":root api:sparqlEndpoint <http://example.com/sparql>."
			+ "\n:root api:endpoint :ep."
			+ "\n:ep a api:ListEndpoint; api:uriTemplate '/alpha/beta'."
			+ "\n:ep api:viewer :v."
			+ "\n:v a api:Viewer; api:name 'v'; api:include :va, :vb." 
			+ "\n:va a api:Viewer; api:name 'va'; api:properties 'a'."
			+ "\n:vb a api:Viewer; api:name 'vb'; api:properties 'b'."
			+ "\n:A a rdf:Property; rdfs:label 'a'."
			+ "\n:B a rdf:Property; rdfs:label 'b'."
			);
		Resource root = m.createResource( NS + "root" );
		Resource endpoint = m.createResource( NS + "ep" );
		APISpec as = SpecUtil.specFrom( root );
		APIEndpointSpec es = new APIEndpointSpec( as, as, endpoint );
		assertEquals(
			CollectionUtils.set( new PropertyChain( NS + "A" ), new PropertyChain( NS + "B" ) ),
			es.getView( "v" ).chains() 
		);
	}

}
