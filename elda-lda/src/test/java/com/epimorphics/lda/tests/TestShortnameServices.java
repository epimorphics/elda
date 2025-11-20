/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.query.ValTranslator;
import com.epimorphics.lda.query.ValTranslator.Filters;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.tests_support.LoadsNothing;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Some (limited) tests for shortname services
 *
 * @author chris
 */
public class TestShortnameServices {

    static final String EX = "http://www.epimorphics.com/tools/example#";

    static final String XSDinteger = XSDDatatype.XSDinteger.getURI();

    @Test
    public void testLanguageDoesNotOverrideType() {
        Model m = ModelIOUtils.modelFromTurtle(":root a api:API. <eh:/P> a owl:DatatypeProperty; api:name 'P'; rdfs:range xsd:integer.");
        Resource root = m.createResource(EX + "root");
        PrefixMapping pm = PrefixMapping.Factory.create();
        ShortnameService sns = new StandardShortnameService(root, pm, LoadsNothing.instance);
        VarSupply vs = null;
        Filters expressions = null;
        ValTranslator vt = new ValTranslator(vs, expressions, sns);
        Param.Info pInf = Param.make(sns, "P").fullParts()[0];
        Any a = vt.objectForValue(pInf, "17", "en");
        assertEquals(RDFQ.literal("17", "", XSDinteger), a);
    }
}
