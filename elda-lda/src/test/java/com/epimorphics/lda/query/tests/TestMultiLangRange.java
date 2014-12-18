package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests.FakeNamedViews;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.CollectionUtils;

public class TestMultiLangRange {

	@Test public void testMultiLanguageEq() {
		Bindings bindings = MakeData.variables( "" );
		Bindings cc = Bindings.createContext( bindings, MakeData.parseQueryString( "" ) );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "label=eh:/label" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		aq.setDefaultLanguage("en,cy");
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );		
	//
		cq.addFilterFromQuery(Param.make( sns, "label" ), "sticky");
		int n = aq.countVarsAllocated() - 1;
		Variable v = RDFQ.var(APIQuery.PREFIX_VAR + "" + n);
	//
		List<RenderExpression> filters = aq.getFilterExpressions();
				
//		Infix en = RDFQ.infix(v, ">=", RDFQ.literal("sticky", "en", null));
//		Infix cy = RDFQ.infix(v, ">=", RDFQ.literal("sticky", "cy", null));
//		Infix or = RDFQ.infix(en, "||", cy);
		
		RenderExpression strOp = RDFQ.infix(RDFQ.apply("str", v), "=", RDFQ.literal("sticky"));
		RenderExpression langEqEn = RDFQ.infix(RDFQ.apply("lang", v), "=", RDFQ.literal("en"));
		RenderExpression langEqCy = RDFQ.infix(RDFQ.apply("lang", v), "=", RDFQ.literal("cy"));
		
		RenderExpression langOr = RDFQ.infix(langEqEn, "||", langEqCy);
		Infix and = RDFQ.infix(strOp, "&&", langOr);
				
//		System.err.println(">> expected: " + and);
//		System.err.println(">> obtained: " + filters.get(0));
		
		assertEquals(CollectionUtils.list(and), filters);
	}

	@Test public void testMultiLanguageMinEtc() {
		Bindings bindings = MakeData.variables( "" );
		Bindings cc = Bindings.createContext( bindings, MakeData.parseQueryString( "" ) );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "label=eh:/label" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		aq.setDefaultLanguage("en,cy");
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );		
	//
		cq.addFilterFromQuery(Param.make( sns, "min-label" ), "sticky");
		int n = aq.countVarsAllocated() - 1;
		Variable v = RDFQ.var(APIQuery.PREFIX_VAR + "label_" + n);
	//
		List<RenderExpression> filters = aq.getFilterExpressions();
//				
//		Infix en = RDFQ.infix(v, ">=", RDFQ.literal("sticky", "en", null));
//		Infix cy = RDFQ.infix(v, ">=", RDFQ.literal("sticky", "cy", null));
//		Infix or = RDFQ.infix(en, "||", cy);
		
		RenderExpression strOp = RDFQ.infix(RDFQ.apply("str", v), ">=", RDFQ.literal("sticky"));
		RenderExpression langEqEn = RDFQ.infix(RDFQ.apply("lang", v), "=", RDFQ.literal("en"));
		RenderExpression langEqCy = RDFQ.infix(RDFQ.apply("lang", v), "=", RDFQ.literal("cy"));
		
		RenderExpression langOr = RDFQ.infix(langEqEn, "||", langEqCy);
		
		Infix and = RDFQ.infix(strOp, "&&", langOr);
		
//		System.err.println(">> expected: " + and);
//		System.err.println(">> obtained: " + filters.get(0));
		
		assertEquals(CollectionUtils.list(and), filters);
	}
}
