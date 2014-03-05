package com.epimorphics.lda.query.tests;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.cache.Cache.Registry;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests.SNS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestQueryTemplate {
	
	static final APISpec spec = trivialSpec();
	
	String NS = "http://www.epimorphics.com/tools/example#";

	@Test public void test_template_generates_correct_query() {
		APIQuery q = QueryTestUtils.queryFromSNS( new SNS( "" ) );
		q.addSubjectHasProperty( RDF.type, RDFQ.var( "?type" ) );
		View v = View.newTemplateView( "_template", "?item <{{NS}}hasData> ?val.".replace("{{NS}}", NS ) );
		APIResultSet rs = q.runQuery
			( new NoteBoard()
			, new Controls()
			, spec
			, Registry.cacheFor( "default", spec.getDataSource() )
			, new Bindings()
			, v
			);
		ModelTestBase.assertIsoModels( expectedModel(), rs.getMergedModel() );
	}
	
	private Model expectedModel() {
		return ModelIOUtils.modelFromTurtle
			( ":dataA :hasData 'A'."
			+ "\n:dataX :hasData 'X'."
			+ "\n"
			);
	}

	static APISpec trivialSpec() {
		Model m = ModelIOUtils.modelFromTurtle
			( ":spec a api:API"
			+ "\n; api:sparqlEndpoint <here:data>"
			+ "\n."
			+ "\n<here:data> :roots :dataA, :dataX"
			+ "\n."
			+ "\n:dataA a :Answer; :hasData 'A'; :hasDodo 'B'"
			+ "\n."
			+ "\n:dataX a :Answer; :hasData 'X'; :hasDodo 'Y'"
			+ "\n."
			+ "\n"
			);
		Resource root = m.getResource( m.expandPrefix( ":spec" ) );
		return new APISpec(EldaFileManager.get(), root, null);
	}
	
}
