package com.epimorphics.lda.slow_mapping_query.tests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.epimorphics.lda.systemtest.Util;
import com.epimorphics.lda.testing.utils.TomcatTestBase;
import com.epimorphics.lda.vocabularies.API;
import org.apache.jena.rdf.model.*;

public class TestMappingQuery extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}
	
	@Test public void testMappingQuery() throws ClientProtocolException, IOException {
		Util.testHttpRequest( "games.ttl?_metadata=all", 200, checkContent );
	}
	
	public static Util.CheckContent checkContent = new Util.CheckContent() {
		
		@Override public String failMessage() {
			return "unexpected failure in mapping query result";
		}
		
		Literal expected = ResourceFactory.createPlainLiteral("http://epimorphics.com/public/vocabulary/games.ttl#martin-wallace");
				
		// [api:label "var_A"; api:value <martin-wallace-URI>]
		@Override public boolean check(String s) {
			
			Model m = ModelFactory.createDefaultModel();
			m.read(new StringReader(s), "", "TTL");

			List<Statement> candidates = m.listStatements(null, API.label, "var_A").toList();
			if (candidates.size() == 1) {
				Resource S = candidates.get(0).getSubject();
				List<Statement> results = S.listProperties(API.value).toList();
				if (results.size() == 1) {
					RDFNode mw = results.get(0).getObject();
					if (mw.equals(expected)) {
						return true;
					} else {						
						fail("expected var_A to be " + expected);
					}
				} else {
					fail("failed to find exactly one api:value");
				}
			} else {
				fail("failed to find exactly one api:label var_A");
			}
			return false;
		}
	};
}
