/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        TestAPI.java
    Created by:  Dave Reynolds
    Created on:  5 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.tests;

import static org.junit.Assert.assertTrue;

import java.io.*;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestAPI {

    protected static final String TEST_BASE = "src/test/resources/api/";
    protected APITester tester = new APITester( TEST_BASE + "apiSpecTest1.ttl");
    protected String apiName = "api1";

    public APIResultSet testAPI(String uri, String query, String expectedResults) {
		Cache.Registry.clearAll();
        String description = "# Model from test: " + uri + "?" + query + "\n";
        APIResultSet rs = tester.runQuery(uri, query);
//        System.err.println( ">> QUERY: " + rs.getSelectQuery() );
        Model rsm = rs.getMergedModel();
        if (expectedResults == null) {
            System.out.print(description);
            rsm.write(System.out, "Turtle");
        } else {
            try {
                Model expected = EldaFileManager.get().loadModel(TEST_BASE + expectedResults);
                if ( ! compareNormalized(expected, rsm)) {
                    // Print out to help debugging
//                    System.out.println(">>  FAILED output for " + description + " [" + expectedResults + "]");
//                    rs.getModel().write(System.out, "Turtle");
//                    System.out.println( ">> EXPECTED:" );
//                    expected.write(System.out, "Turtle" );
                    assertTrue("Failed model comparison for " + expectedResults, false);
                }
            } catch (NotFoundException e) {
                // Create the data file for comparison with future runs
                File file = new File(TEST_BASE, expectedResults);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(description.getBytes());
                    rsm.write(out, "Turtle");
                    out.close();
                } catch (IOException ei) {
                    System.err.println("Failed to create output file: " + file + " " + ei.getMessage());
                }
                
            }
            
            // Also generate a JSON output file
            String jsonFileName = expectedResults.replace(".ttl", ".json");
            File file = new File(TEST_BASE, jsonFileName);
            if ( ! file.exists()) {
                String render = tester.renderAsJSON(apiName, rs);
                try {
                    FileWriter writer = new FileWriter(file);
                    writer.append(description);
                    writer.append(render);
                    writer.close();
                } catch (IOException ei) {
                    System.err.println("Failed to create output file: " + file + " " + ei.getMessage());
                }
            }
        }
        return rs;
    }
    
    private boolean compareNormalized(Model a, Model b) {
        Model na = normalizeModel(a);
		Model nb = normalizeModel(b);
		
		boolean isIso = na.isIsomorphicWith( nb );
		if (!isIso)
			{
			System.err.println( ">> beforehand: " + na.isIsomorphicWith( nb ) );

			Model shared = na.intersection(nb); 
			Model nams = na.difference(shared);
			Model nbms = nb.difference(shared);
			System.err.println( ">> shared:" );
			shared.write( System.err, "Turtle" );
			System.err.println( ">> expected [-shared]: " );
			nams.write(System.err, "Turtle" );
			System.err.println( ">> computed [-shared]: " );
			nbms.write(System.err, "Turtle" );
			System.err.println( ">> afterhand: " + na.difference( shared ).isIsomorphicWith( nb.difference( shared ) ) );
			}
		return isIso;
    }

	private Model normalizeModel(Model m) {
        Model norm = ModelFactory.createDefaultModel();
        norm.add(m);
        // Filter out labels which we may or may not be fetching
        for (StmtIterator i = norm.listStatements(null, RDFS.label, (RDFNode)null); i.hasNext();) {
            i.next();
            i.remove();
        }
        return norm;
    }
    
    @Test
    public void testBasic() {
        // Simple call, uses templates and type filter
        testAPI("http://dummy/doc/schools", "", "testBasicPage0.ttl");
        // test paging on top of that
        testAPI("http://dummy/doc/schools", "_page=1", "testBasicPage1.ttl");
    }
    
    @Test
    public void testFilterQueries() {
        // string case
        testAPI("http://dummy/doc/schools", "name=Name10", "testFilterName10.ttl");
        
        // integer case
        testAPI("http://dummy/doc/schools", "size=200&_view=all", "testFilterSize200.ttl");
        
        // named resource case
        testAPI("http://dummy/doc/schools", "school_type=Primary&_view=all", "testFilterPrimary.ttl");
        
        // Simple path expression
        testAPI("http://dummy/doc/schools", "localAuthority.label=BANES&_view=all", "testFilterPath.ttl");
        
    }
    
    @Test
    public void testFilterInSpec() {
        testAPI("http://dummy/doc/schools/primary", "", "testSpecPrimary.ttl");
        
        testAPI("http://dummy/doc/schools/primary/big", "", "testSpecPrimaryBig.ttl");
            // this also test inheritance
        
        testAPI("http://dummy/doc/schools/london", "", "testSpecLondon.ttl");
    }
    
    @Test
    public void testNameQuery() {
        // Also tests named views
        testAPI("http://dummy/doc/schools", "_view=medium&name-localAuthority=BANES", "testNameAndView.ttl");
    }
    
    @Test
    public void testRangeQueries() {
        testAPI("http://dummy/doc/schools", "min-size=300&_view=all",  "testMin.ttl");
        testAPI("http://dummy/doc/schools", "minEx-size=300&_view=all", "testMinO.ttl");
        testAPI("http://dummy/doc/schools", "max-size=200&_view=all",  "testMax.ttl");
        testAPI("http://dummy/doc/schools", "maxEx-size=200&_view=all", "testMaxO.ttl");
    }

    @Test
    public void testURLTemplateQuery() {
        testAPI("http://dummy/doc/schools/type/Primary", "", "testTypePrimary.ttl");
    }

    @Test
    public void testURLTemplateQueryLiteral() {
        testAPI("http://dummy/doc/schools/la/BANES", "", "testLABanes.ttl");
        // Same thing expressed using where clause, revealed a bug in variable binding for where clauses
        testAPI("http://dummy/doc/schools/laWhere/BANES", "", "testLAWhereBanes.ttl");
    }
    
    @Test
    public void testQueryTemplates() {
    	testAPI("http://dummy/doc/schools", "_properties=name,size,rdf_type", "testTemplateNameSizeType.ttl");
        testAPI("http://dummy/doc/schools", "_properties=name,size", "testTemplateNameSize.ttl");
    }
    
    @Test
    public void testOrderInQuery() {
        testAPI("http://dummy/doc/schools/london", "_sort=size", "testSpecLondonSize.ttl");
        testAPI("http://dummy/doc/schools/london", "_sort=-size", "testSpecLondonSizeDown.ttl");
    }
    
    @Test
    public void testPrefixedReferences() {
        testAPI("http://dummy/doc/schools", "_view=all&localAuthority.rdfs_label=London", "testPrefixedPath.ttl");
    }
    
    @Test
    public void testJSONInteractionBug() {
        APIResultSet rs = testAPI("http://dummy/doc/schools", "size=200&_view=all", "testFilterSize200.ttl");
        tester.renderAsJSON(apiName, rs);
        testAPI("http://dummy/doc/schools", "school_type=Primary&_view=all", "testFilterPrimary.ttl");
    }

    @Test
    public void testSingleSubjectQuery() {
//        testAPI("http://dummy/doc/schools", "_subject=ex_school_1&_view=all", "testSchool1.ttl");
        
        // Note the test harness doesn't need the URL encoding
        testAPI("http://dummy/doc/schools", "_subject=http://www.epimorphics.com/examples/eg1#school_1&_view=all", "testSchool1.ttl");
    }
    
    @Test @Ignore
    public void testExpansionTemplates() {
        testAPI("http://dummy/doc/schools", "_properties=name,size", "testTemplateNameSize.ttl");
        System.err.println( "(textExpansionTemplates suppressed at the moment)" );
        testAPI("http://dummy/doc/schools", "_properties=localAuthority.*", "testExpansion.ttl");
    }
    
    @Test
    public void testExists() {
        testAPI("http://dummy/doc/schools", "_properties=rdf_type,name,size,school_type&size=100&_pageSize=50&exists-school_type=true", "testExistsType.ttl");
    }
    
    @Test
    public void testLanguage() {
    	testAPI("http://dummy/doc/deptsEn", "name=department", "testDeptEn.ttl");
    	testAPI("http://dummy/doc/deptsFr", "name=département", "testDeptFr.ttl");
    }
    
    @Test
    public void testTypedURLParams() {
    	testAPI("http://dummy/doc/schools/size/400", "", "testSizeParam.ttl");
    }
}

