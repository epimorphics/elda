/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * Constants etc for defining test fixtures
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class Fixtures
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    public static final Model PAGE_BWQ_MODEL;
    public static final Model PAGE_BWQ_PROPERTIES_MODEL;
    static {
        PAGE_BWQ_MODEL = FileManager.get().loadModel( "src/test/resources/renderers/page_bwq_fixture.ttl" );
        PAGE_BWQ_PROPERTIES_MODEL = FileManager.get().loadModel( "src/test/resources/renderers/page_bwq_with_properties_fixture.ttl" );
    }
    
    /** Prefixes - the usual suspects */
    public static final String COMMON_PREFIXES =
            "@prefix os:    <http://a9.com/-/spec/opensearch/1.1/> .\n" +
            "@prefix opmv:  <http://purl.org/net/opmv/types/common#> .\n" +
            "@prefix bw:    <http://environment.data.gov.uk/id/bathing-water/> .\n" +
            "@prefix qb:    <http://purl.org/linked-data/cube#> .\n" +
            "@prefix sparql: <http://purl.org/net/opmv/types/sparql#> .\n" +
            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix elda:  <http://www.epimorphics.com/vocabularies/lda#> .\n" +
            "@prefix dct:   <http://purl.org/dc/terms/> .\n" +
            "@prefix def-bwq: <http://environment.data.gov.uk/def/bathing-water-quality/> .\n" +
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix api:   <http://purl.org/linked-data/api/vocab#> .\n" +
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix xhv:   <http://www.w3.org/1999/xhtml/vocab#> .\n" +
            "@prefix doap:  <http://usefulinc.com/ns/doap#> .\n" +
            "@prefix hello: <http://epimorphics.com/public/vocabulary/games.ttl#> .\n" +
            "@prefix skos: <http://www.w3.org/2004/02/skos/core#>.\n";

    /** A page of bathing water results */
    public static final String PAGE_BWQ =
            "def-bwq:SampleAssessment\n" +
            "        rdfs:label  \"AsesiadSampl\"@cy , \"SampleAssessment\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04000/date/20140710/time/114000/recordDate/20140710>\n" +
            "        a                             def-bwq:SampleAssessment , qb:Observation ;\n" +
            "        rdfs:label                    \"Low Newton sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2101-04000 ;\n" +
            "        def-bwq:escherichiaColiCount  18 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T11:40:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04000> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000005> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04200/date/20140710/time/120000/recordDate/20140710>\n" +
            "        a                             def-bwq:SampleAssessment , qb:Observation ;\n" +
            "        rdfs:label                    \"Warkworth sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2101-04200 ;\n" +
            "        def-bwq:escherichiaColiCount  136 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/I> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T12:00:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04200> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000006> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/def/bwq-cc-2012/I>\n" +
            "        rdfs:label  \"Minimum\"@en , \"Isafswm\"@cy .\n" +
            "\n" +
            "<http://reference.data.gov.uk/id/year/2014>\n" +
            "        rdfs:label  \"British Year:2014\"@en .\n" +
            "\n" +
            "<http://reference.data.gov.uk/id/week/2014-W28>\n" +
            "        rdfs:label  \"British Week:2014-W28\"@en .\n" +
            "\n" +
            "bw:ukc2106-04400  rdfs:label  \"Newbiggin North\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03700/date/20140710/time/104000/recordDate/20140710>\n" +
            "        a                             qb:Observation , def-bwq:SampleAssessment ;\n" +
            "        rdfs:label                    \"Bamburgh Castle sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2102-03700 ;\n" +
            "        def-bwq:escherichiaColiCount  27 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T10:40:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03700> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000002> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "bw:ukc2101-04250  rdfs:label  \"Amble Links\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04250>\n" +
            "        rdfs:label  \"Sampling point at Amble Links\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03900>\n" +
            "        rdfs:label  \"Sampling point at Beadnell\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/def/bwq-cc-2012/G>\n" +
            "        rdfs:label  \"Higher\"@en , \"Uchaf\"@cy .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03800>\n" +
            "        rdfs:label  \"Sampling point at Seahouses North\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03800/date/20140710/time/105800/recordDate/20140710>\n" +
            "        a                             qb:Observation , def-bwq:SampleAssessment ;\n" +
            "        rdfs:label                    \"Seahouses North sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2102-03800 ;\n" +
            "        def-bwq:escherichiaColiCount  10 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T10:58:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03800> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000003> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "bw:ukc2101-04200  rdfs:label  \"Warkworth\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04300/date/20140710/time/103500/recordDate/20140710>\n" +
            "        a                             def-bwq:SampleAssessment , qb:Observation ;\n" +
            "        rdfs:label                    \"Druridge Bay South sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2104-04300 ;\n" +
            "        def-bwq:escherichiaColiCount  10 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T10:35:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04300> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000009> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "bw:ukc2104-04300  rdfs:label  \"Druridge Bay South\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04400>\n" +
            "        rdfs:label  \"Sampling point at Newbiggin North\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03700>\n" +
            "        rdfs:label  \"Sampling point at Bamburgh Castle\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04300>\n" +
            "        rdfs:label  \"Sampling point at Druridge Bay South\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03600>\n" +
            "        rdfs:label  \"Sampling point at Spittal\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03600/date/20140710/time/100000/recordDate/20140710>\n" +
            "        a                             def-bwq:SampleAssessment , qb:Observation ;\n" +
            "        rdfs:label                    \"Spittal sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2102-03600 ;\n" +
            "        def-bwq:escherichiaColiCount  127 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/I> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T10:00:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03600> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000001> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "def-bwq:new  rdfs:label  \"newydd\"@cy , \"new\"@en .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04200>\n" +
            "        rdfs:label  \"Sampling point at Warkworth\"@en .\n" +
            "\n" +
            "bw:ukc2101-04000  rdfs:label  \"Low Newton\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04400/date/20140710/time/100000/recordDate/20140710>\n" +
            "        a                             qb:Observation , def-bwq:SampleAssessment ;\n" +
            "        rdfs:label                    \"Newbiggin North sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2106-04400 ;\n" +
            "        def-bwq:escherichiaColiCount  55 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                91 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T10:00:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04400> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000010> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04000>\n" +
            "        rdfs:label  \"Sampling point at Low Newton\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03900/date/20140710/time/111200/recordDate/20140710>\n" +
            "        a                             def-bwq:SampleAssessment , qb:Observation ;\n" +
            "        rdfs:label                    \"Beadnell sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2102-03900 ;\n" +
            "        def-bwq:escherichiaColiCount  10 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T11:12:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/03900> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000004> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season>\n" +
            "        rdfs:label  \"Bathing Water Quality - In-Season Assessment Dataset (eaew).\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04280/date/20140710/time/111000/recordDate/20140710>\n" +
            "        a                             qb:Observation , def-bwq:SampleAssessment ;\n" +
            "        rdfs:label                    \"Druridge Bay North sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2104-04280 ;\n" +
            "        def-bwq:escherichiaColiCount  10 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T11:10:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04280> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000008> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "bw:ukc2102-03900  rdfs:label  \"Beadnell\"@en .\n" +
            "\n" +
            "bw:ukc2102-03800  rdfs:label  \"Seahouses North\"@en .\n" +
            "\n" +
            "def-bwq:actual  rdfs:label  \"gwirioneddol\"@cy , \"actual\"@en .\n" +
            "\n" +
            "bw:ukc2102-03700  rdfs:label  \"Bamburgh Castle\"@en .\n" +
            "\n" +
            "def-bwq:lessThan  rdfs:label  \"llai-na\"@cy , \"less-than\"@en .\n" +
            "\n" +
            "bw:ukc2102-03600  rdfs:label  \"Spittal\"@en .\n" +
            "\n" +
            "bw:ukc2104-04280  rdfs:label  \"Druridge Bay North\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04250/date/20140710/time/113000/recordDate/20140710>\n" +
            "        a                             def-bwq:SampleAssessment , qb:Observation ;\n" +
            "        rdfs:label                    \"Amble Links sample assessment for 2014-W28\"@en ;\n" +
            "        def-bwq:abnormalWeatherException\n" +
            "                false ;\n" +
            "        def-bwq:bathingWater          bw:ukc2101-04250 ;\n" +
            "        def-bwq:escherichiaColiCount  55 ;\n" +
            "        def-bwq:escherichiaColiQualifier\n" +
            "                def-bwq:actual ;\n" +
            "        def-bwq:intestinalEnterococciCount\n" +
            "                10 ;\n" +
            "        def-bwq:intestinalEnterococciQualifier\n" +
            "                def-bwq:lessThan ;\n" +
            "        def-bwq:recordDate            \"2014-07-15Z\"^^xsd:date ;\n" +
            "        def-bwq:recordStatus          def-bwq:new ;\n" +
            "        def-bwq:sampleClassification  <http://environment.data.gov.uk/def/bwq-cc-2012/G> ;\n" +
            "        def-bwq:sampleDateTime        <http://reference.data.gov.uk/id/gregorian-instant/2014-07-10T11:30:00> ;\n" +
            "        def-bwq:sampleWeek            <http://reference.data.gov.uk/id/week/2014-W28> ;\n" +
            "        def-bwq:sampleYear            <http://reference.data.gov.uk/id/year/2014> ;\n" +
            "        def-bwq:samplingPoint         <http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04250> ;\n" +
            "        dct:created                   \"2014-07-15Z\"^^xsd:date ;\n" +
            "        dct:source                    <http://environment.data.gov.uk/sources/bwq/eaew/input/in-season-ea_bw_sample_update_20140715-20140715-073353-67.csv#line=000007> ;\n" +
            "        qb:dataSet                    <http://environment.data.gov.uk/data/bathing-water-quality/eaew/in-season> .\n" +
            "\n" +
            "<http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/04280>\n" +
            "        rdfs:label  \"Sampling point at Druridge Bay North\"@en .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.json?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        rdfs:label      \"json\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/json\" ] ;\n" +
            "        dct:isFormatOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=assessment&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"assessment\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"bwq_samplingPoint.samplePointNotation\" , \"totalColiformCount\" , \"sampleClassification.complianceCodeNotation\" , \"entrovirusQualifier.countQualifierNotation\" , \"escherichiaColiQualifier.countQualifierNotation\" , \"salmonellaPresent.presenceNotation\" , \"intestinalEnterococciQualifier.countQualifierNotation\" , \"faecalStreptococciQualifier.countQualifierNotation\" , \"escherichiaColiCount\" , \"faecalColiformCount\" , \"abnormalWeatherException\" , \"faecalStreptococciCount\" , \"sampleDateTime.inXSDDateTime\" , \"intestinalEnterococciCount\" , \"source\" , \"sampleWeek.label\" , \"bwq_samplingPoint.name\" , \"salmonellaPresent.name\" , \"bwq_bathingWater.name\" , \"dataset\" , \"type\" , \"totalColiformQualifier.countQualifierNotation\" , \"faecalColiformQualifier.countQualifierNotation\" , \"entrovirusCount\" , \"label\" , \"sampleClassification.name\" , \"bwq_bathingWater.eubwidNotation\" ;\n" +
            "        elda:viewName    \"assessment\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.text?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        rdfs:label      \"text\" ;\n" +
            "        dct:format      [ rdfs:label  \"text/plain\" ] ;\n" +
            "        dct:isFormatOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.html?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        rdfs:label      \"html\" ;\n" +
            "        dct:format      [ rdfs:label  \"text/html\" ] ;\n" +
            "        dct:isFormatOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=location&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"location\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"label\" , \"source\" , \"bwq_bathingWater.eubwidNotation\" , \"totalColiformQualifier.countQualifierNotation\" , \"escherichiaColiQualifier.countQualifierNotation\" , \"dataset\" , \"entrovirusCount\" , \"faecalColiformCount\" , \"bwq_samplingPoint.northing\" , \"faecalColiformQualifier.countQualifierNotation\" , \"sampleDateTime.inXSDDateTime\" , \"sampleWeek.label\" , \"type\" , \"sampleClassification.complianceCodeNotation\" , \"bwq_samplingPoint.easting\" , \"intestinalEnterococciQualifier.countQualifierNotation\" , \"bwq_bathingWater.name\" , \"bwq_samplingPoint.samplePointNotation\" , \"entrovirusQualifier.countQualifierNotation\" , \"bwq_samplingPoint.long\" , \"escherichiaColiCount\" , \"intestinalEnterococciCount\" , \"bwq_samplingPoint.lat\" , \"faecalStreptococciQualifier.countQualifierNotation\" , \"faecalStreptococciCount\" , \"salmonellaPresent.presenceNotation\" , \"totalColiformCount\" , \"sampleClassification.name\" , \"salmonellaPresent.name\" , \"bwq_samplingPoint.name\" , \"abnormalWeatherException\" ;\n" +
            "        elda:viewName    \"location\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"all\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        elda:viewName    \"all\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=streptococci&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"streptococci\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"type\" , \"bwq_bathingWater.name\" , \"bwq_samplingPoint.name\" , \"source\" , \"sampleWeek.label\" , \"bwq_samplingPoint.samplePointNotation\" , \"sampleDateTime.inXSDDateTime\" , \"bwq_bathingWater.eubwidNotation\" , \"dataset\" , \"faecalStreptococciQualifier.countQualifierNotation\" , \"faecalStreptococciCount\" , \"label\" , \"abnormalWeatherException\" ;\n" +
            "        elda:viewName    \"streptococci\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=description&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"description\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        elda:viewName    \"description\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.xml?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        rdfs:label      \"xml\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/xml\" ] ;\n" +
            "        dct:isFormatOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> .\n" +
            "\n" +
            "_:b0    a                   <http://www.w3.org/ns/sparql-service-description#Service> ;\n" +
            "        api:sparqlEndpoint  <http://localhost:3030/bwq/query> ;\n" +
            "        <http://www.w3.org/ns/sparql-service-description#url>\n" +
            "                <http://localhost:3030/bwq/query> .\n" +
            "\n" +
            "<https://elda.googlecode.com/hg/>\n" +
            "        a              doap:Repository ;\n" +
            "        doap:browse    <http://code.google.com/p/elda/source/browse/> ;\n" +
            "        doap:location  <https://elda.googlecode.com> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=basic&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"basic\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"type\" , \"label\" ;\n" +
            "        elda:viewName    \"basic\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=intestinalEnterococci&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"intestinalEnterococci\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"bwq_samplingPoint.name\" , \"bwq_samplingPoint.samplePointNotation\" , \"bwq_bathingWater.name\" , \"intestinalEnterococciQualifier.countQualifierNotation\" , \"source\" , \"label\" , \"sampleWeek.label\" , \"bwq_bathingWater.eubwidNotation\" , \"abnormalWeatherException\" , \"sampleDateTime.inXSDDateTime\" , \"intestinalEnterococciCount\" , \"type\" , \"dataset\" ;\n" +
            "        elda:viewName    \"intestinalEnterococci\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=coliforms&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"coliforms\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"bwq_bathingWater.eubwidNotation\" , \"abnormalWeatherException\" , \"bwq_samplingPoint.name\" , \"faecalColiformQualifier.countQualifierNotation\" , \"label\" , \"bwq_bathingWater.name\" , \"type\" , \"sampleWeek.label\" , \"dataset\" , \"sampleDateTime.inXSDDateTime\" , \"source\" , \"totalColiformCount\" , \"bwq_samplingPoint.samplePointNotation\" , \"faecalColiformCount\" , \"totalColiformQualifier.countQualifierNotation\" ;\n" +
            "        elda:viewName    \"coliforms\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all>\n" +
            "        a               api:ListEndpoint ;\n" +
            "        dct:hasPart     <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:definition  <http://environment.data.gov.uk/meta/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.csv?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        rdfs:label      \"csv\" ;\n" +
            "        dct:format      [ rdfs:label  \"text/csv\" ] ;\n" +
            "        dct:isFormatOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=entrovirus&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"entrovirus\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"dataset\" , \"abnormalWeatherException\" , \"type\" , \"entrovirusQualifier.countQualifierNotation\" , \"sampleWeek.label\" , \"bwq_bathingWater.name\" , \"bwq_samplingPoint.samplePointNotation\" , \"bwq_samplingPoint.name\" , \"source\" , \"sampleDateTime.inXSDDateTime\" , \"bwq_bathingWater.eubwidNotation\" , \"entrovirusCount\" , \"label\" ;\n" +
            "        elda:viewName    \"entrovirus\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=assessment-csv&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"assessment-csv\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"sampleClassification.complianceCodeNotation\" , \"entrovirusQualifier.countQualifierNotation\" , \"bwq_samplingPoint.samplePointNotation\" , \"totalColiformQualifier.countQualifierNotation\" , \"sampleDateTime.inXSDDateTime\" , \"faecalColiformQualifier.countQualifierNotation\" , \"intestinalEnterococciCount\" , \"sampleWeek.label\" , \"abnormalWeatherException\" , \"escherichiaColiCount\" , \"totalColiformCount\" , \"bwq_bathingWater.name\" , \"faecalStreptococciCount\" , \"escherichiaColiQualifier.countQualifierNotation\" , \"faecalColiformCount\" , \"faecalStreptococciQualifier.countQualifierNotation\" , \"entrovirusCount\" , \"salmonellaPresent.presenceNotation\" , \"intestinalEnterococciQualifier.countQualifierNotation\" , \"bwq_bathingWater.eubwidNotation\" ;\n" +
            "        elda:viewName    \"assessment-csv\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.rdf?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        rdfs:label      \"rdf\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/rdf+xml\" ] ;\n" +
            "        dct:isFormatOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        a                            api:Page ;\n" +
            "        rdfs:label                   \"ttl\" ;\n" +
            "        os:itemsPerPage              \"10\"^^xsd:long ;\n" +
            "        os:startIndex                \"1\"^^xsd:long ;\n" +
            "        dct:format                   [ rdfs:label\n" +
            "                          \"text/turtle\" ] ;\n" +
            "        dct:hasFormat                <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.text?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.rdf?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.csv?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.xml?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.html?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.json?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        dct:hasVersion               <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=assessment-csv&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=description&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=basic&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=streptococci&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=coliforms&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=location&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=assessment&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=salmonella&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=classification&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=entrovirus&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=escherichiaColi&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=intestinalEnterococci&_metadata=all&_page=0> ;\n" +
            "        dct:isFormatOf               <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        dct:isPartOf                 <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all> ;\n" +
            "        api:definition               <http://environment.data.gov.uk/meta/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all> ;\n" +
            "        api:extendedMetadataVersion  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=0> ;\n" +
            "        api:items                    ( <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03600/date/20140710/time/100000/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03700/date/20140710/time/104000/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03800/date/20140710/time/105800/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03900/date/20140710/time/111200/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04000/date/20140710/time/114000/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04200/date/20140710/time/120000/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04250/date/20140710/time/113000/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04280/date/20140710/time/111000/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04300/date/20140710/time/103500/recordDate/20140710> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04400/date/20140710/time/100000/recordDate/20140710> ) ;\n" +
            "        api:page                     \"0\"^^xsd:long ;\n" +
            "        api:wasResultOf              [ a                    api:Execution ;\n" +
            "                                       api:processor        [ a              api:Service ;\n" +
            "                                                              opmv:software  elda:Elda_1.2.33\n" +
            "                                                            ] ;\n" +
            "                                       api:selectionResult  [ a                sparql:QueryResult ;\n" +
            "                                                              sparql:endpoint  _:b0 ;\n" +
            "                                                              sparql:query     [ rdf:value  \"PREFIX bwq-iss: <http://environment.data.gov.uk/data/bathing-water-quality/in-season/slice/>\\nPREFIX def-bwq: <http://environment.data.gov.uk/def/bathing-water-quality/>\\nPREFIX def-ef: <http://location.data.gov.uk/def/ef/SamplingPoint/>\\nPREFIX qb: <http://purl.org/linked-data/cube#>\\nSELECT DISTINCT ?item\\nWHERE {\\nbwq-iss:latest qb:observation ?item.OPTIONAL { ?item def-bwq:samplingPoint ?___1 . ?___1 def-ef:samplePointNotation ?___0 . }\\n}  ORDER BY  ?___0  ?item OFFSET 0 LIMIT 10\" ]\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"new\" ;\n" +
            "                                                              api:property  def-bwq:new\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"created\" ;\n" +
            "                                                              api:property  dct:created\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleWeek\" ;\n" +
            "                                                              api:property  def-bwq:sampleWeek\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"SampleAssessment\" ;\n" +
            "                                                              api:property  def-bwq:SampleAssessment\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"bwq_bathingWater\" ;\n" +
            "                                                              api:property  def-bwq:bathingWater\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleDateTime\" ;\n" +
            "                                                              api:property  def-bwq:sampleDateTime\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"api_definition\" ;\n" +
            "                                                              api:property  api:definition\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"escherichiaColiCount\" ;\n" +
            "                                                              api:property  def-bwq:escherichiaColiCount\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"dataset\" ;\n" +
            "                                                              api:property  qb:dataSet\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"escherichiaColiQualifier\" ;\n" +
            "                                                              api:property  def-bwq:escherichiaColiQualifier\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"first\" ;\n" +
            "                                                              api:property  xhv:first\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"bwq_samplingPoint\" ;\n" +
            "                                                              api:property  def-bwq:samplingPoint\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"hasPart\" ;\n" +
            "                                                              api:property  dct:hasPart\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"isPartOf\" ;\n" +
            "                                                              api:property  dct:isPartOf\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleYear\" ;\n" +
            "                                                              api:property  def-bwq:sampleYear\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"recordDate\" ;\n" +
            "                                                              api:property  def-bwq:recordDate\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"itemsPerPage\" ;\n" +
            "                                                              api:property  os:itemsPerPage\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"extendedMetadataVersion\" ;\n" +
            "                                                              api:property  api:extendedMetadataVersion\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"startIndex\" ;\n" +
            "                                                              api:property  os:startIndex\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"items\" ;\n" +
            "                                                              api:property  api:items\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"Observation\" ;\n" +
            "                                                              api:property  qb:Observation\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"type\" ;\n" +
            "                                                              api:property  rdf:type\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"intestinalEnterococciCount\" ;\n" +
            "                                                              api:property  def-bwq:intestinalEnterococciCount\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"next\" ;\n" +
            "                                                              api:property  xhv:next\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"intestinalEnterococciQualifier\" ;\n" +
            "                                                              api:property  def-bwq:intestinalEnterococciQualifier\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"label\" ;\n" +
            "                                                              api:property  rdfs:label\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"source\" ;\n" +
            "                                                              api:property  dct:source\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"recordStatus\" ;\n" +
            "                                                              api:property  def-bwq:recordStatus\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"abnormalWeatherException\" ;\n" +
            "                                                              api:property  def-bwq:abnormalWeatherException\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleClassification\" ;\n" +
            "                                                              api:property  def-bwq:sampleClassification\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"page\" ;\n" +
            "                                                              api:property  api:page\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_suppress_ipto\" ;\n" +
            "                                                              api:value  \"yes\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"bw\" ;\n" +
            "                                                              api:value  \"http://environment.data.gov.uk/id/bathing-water/{eubwid}\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"sp\" ;\n" +
            "                                                              api:value  \"http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/{bwspid}\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_rootPath\" ;\n" +
            "                                                              api:value  \"null\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_suffix\" ;\n" +
            "                                                              api:value  \"ttl\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_lang\" ;\n" +
            "                                                              api:value  \"en,cy\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_page\" ;\n" +
            "                                                              api:value  \"0\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"visibleSparqlEndpoint\" ;\n" +
            "                                                              api:value  \"http://environment.data.gov.uk/sparql/bwq/query\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_HOST\" ;\n" +
            "                                                              api:value  \"environment.data.gov.uk\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_view\" ;\n" +
            "                                                              api:value  \"all\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_metadata\" ;\n" +
            "                                                              api:value  \"all\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_properties\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_APP\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_resourceRoot\" ;\n" +
            "                                                              api:value  \"/lda-assets/\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_strip_has\" ;\n" +
            "                                                              api:value  \"yes\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"visibleSparqlForm\" ;\n" +
            "                                                              api:value  \"http://environment.data.gov.uk/lab/sparql.html\"\n" +
            "                                                            ] ;\n" +
            "                                       api:viewingResult    [ a                sparql:QueryResult ;\n" +
            "                                                              sparql:endpoint  _:b0 ;\n" +
            "                                                              sparql:query     [ rdf:value  \"DESCRIBE\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04250/date/20140710/time/113000/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03800/date/20140710/time/105800/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04400/date/20140710/time/100000/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04280/date/20140710/time/111000/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04000/date/20140710/time/114000/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04300/date/20140710/time/103500/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04200/date/20140710/time/120000/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03700/date/20140710/time/104000/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03900/date/20140710/time/111200/recordDate/20140710>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03600/date/20140710/time/100000/recordDate/20140710>\" ]\n" +
            "                                                            ]\n" +
            "                                     ] ;\n" +
            "        xhv:first                    <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=0> ;\n" +
            "        xhv:next                     <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=1> .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=salmonella&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"salmonella\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"bwq_samplingPoint.name\" , \"type\" , \"bwq_bathingWater.eubwidNotation\" , \"sampleWeek.label\" , \"label\" , \"abnormalWeatherException\" , \"bwq_samplingPoint.samplePointNotation\" , \"bwq_bathingWater.name\" , \"source\" , \"salmonellaPresent.name\" , \"dataset\" , \"salmonellaPresent.presenceNotation\" , \"sampleDateTime.inXSDDateTime\" ;\n" +
            "        elda:viewName    \"salmonella\" .\n" +
            "\n" +
            "elda:Elda_1.2.33  a     doap:Version ;\n" +
            "        rdfs:label      \"Elda 1.2.33\" ;\n" +
            "        doap:releaseOf  [ rdfs:label                 \"Elda\" ;\n" +
            "                          doap:bug-database          <http://code.google.com/p/elda/issues/list> ;\n" +
            "                          doap:homepage              <http://elda.googlecode.com> ;\n" +
            "                          doap:implements            \"http://code.google.com/p/linked-data-api/wiki/Specification\" ;\n" +
            "                          doap:programming-language  \"Java\" ;\n" +
            "                          doap:repository            <https://elda.googlecode.com/hg/> ;\n" +
            "                          doap:wiki                  <http://code.google.com/p/elda/w/list>\n" +
            "                        ] ;\n" +
            "        doap:revision   \"1.2.33\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=classification&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"classification\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"sampleClassification.name\" , \"bwq_samplingPoint.samplePointNotation\" , \"dataset\" , \"bwq_bathingWater.eubwidNotation\" , \"sampleDateTime.inXSDDateTime\" , \"abnormalWeatherException\" , \"sampleClassification.complianceCodeNotation\" , \"bwq_samplingPoint.name\" , \"sampleWeek.label\" , \"bwq_bathingWater.name\" , \"source\" , \"type\" , \"label\" ;\n" +
            "        elda:viewName    \"classification\" .\n" +
            "\n" +
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=escherichiaColi&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"escherichiaColi\" ;\n" +
            "        dct:isVersionOf  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        api:properties   \"type\" , \"sampleDateTime.inXSDDateTime\" , \"sampleWeek.label\" , \"dataset\" , \"bwq_samplingPoint.name\" , \"label\" , \"escherichiaColiCount\" , \"escherichiaColiQualifier.countQualifierNotation\" , \"bwq_bathingWater.name\" , \"source\" , \"bwq_bathingWater.eubwidNotation\" , \"bwq_samplingPoint.samplePointNotation\" , \"abnormalWeatherException\" ;\n" +
            "        elda:viewName    \"escherichiaColi\" .";

    /** Metadata from a BWQ page */
    public static final String PAGE_METADATA_BWQ = "<http://localhost:8080/standalone/bwq/doc/bathing-water?_view=all&_metadata=all&_page=0>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"all\" ;\n" +
            "        <http://purl.org/dc/terms/isVersionOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://www.epimorphics.com/vocabularies/lda#viewName>\n" +
            "                \"all\" .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water?_view=bathing-water&_metadata=all&_page=0>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"bathing-water\" ;\n" +
            "        <http://purl.org/dc/terms/isVersionOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#properties>\n" +
            "                \"country.name\" , \"latestSampleAssessment\" , \"seeAlso\" , \"latestComplianceAssessment\" , \"uriSet.label\" , \"samplingPoint.northing\" , \"samplingPoint.name\" , \"district.name\" , \"latestComplianceAssessment.complianceClassification.name\" , \"yearDedesignated\" , \"sameAs\" , \"type\" , \"county.name\" , \"latestRiskPrediction.riskLevel.name\" , \"zoneOfInfluence.name\" , \"sedimentTypesPresent\" , \"name\" , \"waterQualityImpactedByHeavyRain\" , \"latestRiskPrediction.expiresAt\" , \"eubwidNotation\" , \"envelope.label\" , \"uriSet.name\" , \"samplingPoint.easting\" , \"samplingPoint.long\" , \"latestSampleAssessment.sampleClassification.name\" , \"samplingPoint.lat\" , \"regionalOrganization.name\" , \"yearDesignated\" , \"latestProfile\" ;\n" +
            "        <http://www.epimorphics.com/vocabularies/lda#viewName>\n" +
            "                \"bathing-water\" .\n" +
            "\n" +
            "<http://www.epimorphics.com/vocabularies/lda#Elda_1.2.36-SNAPSHOT>\n" +
            "        a       <http://usefulinc.com/ns/doap#Version> ;\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"Elda 1.2.36-SNAPSHOT\" ;\n" +
            "        <http://usefulinc.com/ns/doap#releaseOf>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"Elda\" ;\n" +
            "                  <http://usefulinc.com/ns/doap#bug-database>\n" +
            "                          <https://github.com/epimorphics/elda/issues?direction=desc&sort=created&state=open> ;\n" +
            "                  <http://usefulinc.com/ns/doap#homepage>\n" +
            "                          <https://github.com/epimorphics/elda> ;\n" +
            "                  <http://usefulinc.com/ns/doap#implements>\n" +
            "                          \"http://code.google.com/p/linked-data-api/wiki/Specification\" ;\n" +
            "                  <http://usefulinc.com/ns/doap#programming-language>\n" +
            "                          \"Java\" ;\n" +
            "                  <http://usefulinc.com/ns/doap#repository>\n" +
            "                          <https://github.com/epimorphics/elda.git> ;\n" +
            "                  <http://usefulinc.com/ns/doap#wiki>\n" +
            "                          <https://github.com/epimorphics/elda/wiki>\n" +
            "                ] ;\n" +
            "        <http://usefulinc.com/ns/doap#revision>\n" +
            "                \"1.2.36-SNAPSHOT\" .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water?_view=prediction&_metadata=all&_page=0>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"prediction\" ;\n" +
            "        <http://purl.org/dc/terms/isVersionOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#properties>\n" +
            "                \"latestRiskPrediction.source\" , \"latestRiskPrediction.type\" , \"latestRiskPrediction.comment\" , \"latestRiskPrediction.riskLevel.name\" , \"latestRiskPrediction.publishedAt\" , \"latestRiskPrediction.predictedAt\" , \"latestRiskPrediction.predictedOn\" , \"latestRiskPrediction.expiresAt\" , \"name\" ;\n" +
            "        <http://www.epimorphics.com/vocabularies/lda#viewName>\n" +
            "                \"prediction\" .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.json?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"json\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"application/json\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all>\n" +
            "        a       <http://purl.org/linked-data/api/vocab#Page> , <http://purl.org/linked-data/api/vocab#ListEndpoint> ;\n" +
            "        <http://a9.com/-/spec/opensearch/1.1/itemsPerPage>\n" +
            "                \"10\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n" +
            "        <http://a9.com/-/spec/opensearch/1.1/startIndex>\n" +
            "                \"1\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n" +
            "        <http://purl.org/dc/terms/hasFormat>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water.xml?_metadata=all> , <http://localhost:8080/standalone/bwq/doc/bathing-water.csv?_metadata=all> , <http://localhost:8080/standalone/bwq/doc/bathing-water.html?_metadata=all> , <http://localhost:8080/standalone/bwq/doc/bathing-water.text?_metadata=all> , <http://localhost:8080/standalone/bwq/doc/bathing-water.ttl?_metadata=all> , <http://localhost:8080/standalone/bwq/doc/bathing-water.rdf?_metadata=all> , <http://localhost:8080/standalone/bwq/doc/bathing-water.json?_metadata=all> ;\n" +
            "        <http://purl.org/dc/terms/hasPart>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/dc/terms/hasVersion>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_view=description&_metadata=all&_page=0> , <http://localhost:8080/standalone/bwq/doc/bathing-water?_view=bathing-water&_metadata=all&_page=0> , <http://localhost:8080/standalone/bwq/doc/bathing-water?_view=basic&_metadata=all&_page=0> , <http://localhost:8080/standalone/bwq/doc/bathing-water?_view=all&_metadata=all&_page=0> , <http://localhost:8080/standalone/bwq/doc/bathing-water?_view=prediction&_metadata=all&_page=0> ;\n" +
            "        <http://purl.org/dc/terms/isPartOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#definition>\n" +
            "                <http://localhost:8080/standalone/bwq/meta/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#extendedMetadataVersion>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#items>\n" +
            "                ( <http://environment.data.gov.uk/id/bathing-water/ukc2102-03600> <http://environment.data.gov.uk/id/bathing-water/ukc2102-03700> <http://environment.data.gov.uk/id/bathing-water/ukc2102-03800> <http://environment.data.gov.uk/id/bathing-water/ukc2102-03900> <http://environment.data.gov.uk/id/bathing-water/ukc2101-04000> <http://environment.data.gov.uk/id/bathing-water/ukc2101-04200> <http://environment.data.gov.uk/id/bathing-water/ukc2101-04250> <http://environment.data.gov.uk/id/bathing-water/ukc2104-04280> <http://environment.data.gov.uk/id/bathing-water/ukc2104-04300> <http://environment.data.gov.uk/id/bathing-water/ukc2106-04400> ) ;\n" +
            "        <http://purl.org/linked-data/api/vocab#page>\n" +
            "                \"0\"^^<http://www.w3.org/2001/XMLSchema#long> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#wasResultOf>\n" +
            "                [ a       <http://purl.org/linked-data/api/vocab#Execution> ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#processor>\n" +
            "                          [ a       <http://purl.org/linked-data/api/vocab#Service> ;\n" +
            "                            <http://purl.org/net/opmv/types/common#software>\n" +
            "                                    <http://www.epimorphics.com/vocabularies/lda#Elda_1.2.36-SNAPSHOT>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#selectionResult>\n" +
            "                          [ a       <http://purl.org/net/opmv/types/sparql#QueryResult> ;\n" +
            "                            <http://purl.org/net/opmv/types/sparql#endpoint>\n" +
            "                                    _:b0 ;\n" +
            "                            <http://purl.org/net/opmv/types/sparql#query>\n" +
            "                                    [ <http://www.w3.org/1999/02/22-rdf-syntax-ns#value>\n" +
            "                                              \"PREFIX def-bw: <http://environment.data.gov.uk/def/bathing-water/>\\nPREFIX def-ef: <http://location.data.gov.uk/def/ef/SamplingPoint/>\\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\\nSELECT DISTINCT ?item\\nWHERE {\\n?item def-ef:samplingPoint ?___samplingPoint_0 .\\n?item rdf:type def-bw:BathingWater .\\nOPTIONAL { ?item def-bw:yearDedesignated ?___1 . }\\nOPTIONAL { ?___samplingPoint_0 def-ef:samplePointNotation ?___2 . }\\n FILTER (!(bound(?___1)))\\n}  ORDER BY  ?___2  ?item OFFSET 0 LIMIT 10\" ]\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"regionalOrganization\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/regionalOrganization>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"itemsPerPage\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://a9.com/-/spec/opensearch/1.1/itemsPerPage>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"startIndex\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://a9.com/-/spec/opensearch/1.1/startIndex>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"latestSampleAssessment\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water-quality/latestSampleAssessment>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"northing\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/northing>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"next\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/1999/xhtml/vocab#next>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"waterQualityImpactedByHeavyRain\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/waterQualityImpactedByHeavyRain>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"normal\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bwq-stp/normal>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"district\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://statistics.data.gov.uk/def/administrative-geography/district>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"riskLevel\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bwq-stp/riskLevel>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"name\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/2004/02/skos/core#prefLabel>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"eubwidNotation\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/eubwidNotation>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"zoneOfInfluence\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://location.data.gov.uk/def/ef/ZoneOfInfluence/zoneOfInfluence>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"sedimentTypesPresent\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/sedimentTypesPresent>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"latestComplianceAssessment\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water-quality/latestComplianceAssessment>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"api_definition\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://purl.org/linked-data/api/vocab#definition>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"easting\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/easting>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"items\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://purl.org/linked-data/api/vocab#items>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"uriSet\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://reference.data.gov.uk/def/reference/uriSet>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"yearDesignated\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/yearDesignated>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"latestRiskPrediction\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bwq-stp/latestRiskPrediction>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"country\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://statistics.data.gov.uk/def/administrative-geography/country>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"sameAs\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/2002/07/owl#sameAs>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"long\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/2003/01/geo/wgs84_pos#long>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"CoastalBathingWater\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/CoastalBathingWater>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"first\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/1999/xhtml/vocab#first>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"samplingPoint\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://location.data.gov.uk/def/ef/SamplingPoint/samplingPoint>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"hasPart\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://purl.org/dc/terms/hasPart>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"sand\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/sand-sediment>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"complianceClassification\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water-quality/complianceClassification>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"isPartOf\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://purl.org/dc/terms/isPartOf>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"extendedMetadataVersion\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://purl.org/linked-data/api/vocab#extendedMetadataVersion>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"page\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://purl.org/linked-data/api/vocab#page>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"expiresAt\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bwq-stp/expiresAt>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"sampleClassification\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water-quality/sampleClassification>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"latestProfile\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water-profile/latestBathingWaterProfile>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"label\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"lat\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/2003/01/geo/wgs84_pos#lat>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"BathingWater\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://environment.data.gov.uk/def/bathing-water/BathingWater>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"type\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#termBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"envelope\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#property>\n" +
            "                                    <http://location.data.gov.uk/def/common/Geometry/envelope>\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_metadata\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"all\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_properties\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"bw\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"http://environment.data.gov.uk/id/bathing-water/{eubwid}\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_velocityRoot\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"assets/velocity\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_rootPath\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"/standalone/bwq\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_strip_has\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"yes\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_HOST\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"localhost:8080\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_view\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_suppress_ipto\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"yes\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_APP\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"/standalone\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"sp\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/{bwspid}\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"visibleSparqlForm\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"http://environment.data.gov.uk/lab/sparql.html\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_page\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_resourceRoot\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"/lda-assets/\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"visibleSparqlEndpoint\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"http://environment.data.gov.uk/sparql/bwq/query\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_selectedView\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"bathing-water\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#variableBinding>\n" +
            "                          [ <http://purl.org/linked-data/api/vocab#label>\n" +
            "                                    \"_suffix\" ;\n" +
            "                            <http://purl.org/linked-data/api/vocab#value>\n" +
            "                                    \"html\"\n" +
            "                          ] ;\n" +
            "                  <http://purl.org/linked-data/api/vocab#viewingResult>\n" +
            "                          [ a       <http://purl.org/net/opmv/types/sparql#QueryResult> ;\n" +
            "                            <http://purl.org/net/opmv/types/sparql#endpoint>\n" +
            "                                    _:b0 ;\n" +
            "                            <http://purl.org/net/opmv/types/sparql#query>\n" +
            "                                    [ <http://www.w3.org/1999/02/22-rdf-syntax-ns#value>\n" +
            "                                              \"PREFIX def-bw: <http://environment.data.gov.uk/def/bathing-water/>\\nPREFIX def-ef: <http://location.data.gov.uk/def/ef/SamplingPoint/>\\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\\nPREFIX def-bw: <http://environment.data.gov.uk/def/bathing-water/>\\nPREFIX def-bwp: <http://environment.data.gov.uk/def/bathing-water-profile/>\\nPREFIX def-bwq: <http://environment.data.gov.uk/def/bathing-water-quality/>\\nPREFIX def-ef: <http://location.data.gov.uk/def/ef/SamplingPoint/>\\nPREFIX def-geom: <http://location.data.gov.uk/def/common/Geometry/>\\nPREFIX def-stp: <http://environment.data.gov.uk/def/bwq-stp/>\\nPREFIX def-zoi: <http://location.data.gov.uk/def/ef/ZoneOfInfluence/>\\nPREFIX dgu: <http://reference.data.gov.uk/def/reference/>\\nPREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\\nPREFIX onsadmingeo: <http://statistics.data.gov.uk/def/administrative-geography/>\\nPREFIX owl: <http://www.w3.org/2002/07/owl#>\\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\\nPREFIX skos: <http://www.w3.org/2004/02/skos/core#>\\nPREFIX spatialrelations: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/>\\nCONSTRUCT {?item def-bwq:latestComplianceAssessment ?___3 .\\n?___3 def-bwq:complianceClassification ?___4 .\\n?___4 skos:prefLabel ?___5 .\\n?item rdfs:seeAlso ?___6 .\\n?item def-bw:sedimentTypesPresent ?___7 .\\n?item def-bw:yearDesignated ?___8 .\\n?item def-bw:eubwidNotation ?___9 .\\n?item def-geom:envelope ?___10 .\\n?___10 rdfs:label ?___11 .\\n?item def-bw:yearDedesignated ?___12 .\\n?item onsadmingeo:county ?___13 .\\n?___13 skos:prefLabel ?___14 .\\n?item def-bwq:latestSampleAssessment ?___15 .\\n?___15 def-bwq:sampleClassification ?___16 .\\n?___16 skos:prefLabel ?___17 .\\n?item dgu:uriSet ?___18 .\\n?___18 rdfs:label ?___19 .\\n?___18 skos:prefLabel ?___20 .\\n?item def-bw:waterQualityImpactedByHeavyRain ?___21 .\\n?item def-stp:latestRiskPrediction ?___22 .\\n?___22 def-stp:riskLevel ?___23 .\\n?___23 skos:prefLabel ?___24 .\\n?___22 def-stp:expiresAt ?___25 .\\n?item onsadmingeo:district ?___26 .\\n?___26 skos:prefLabel ?___27 .\\n?item rdf:type ?___28 .\\n?item onsadmingeo:country ?___29 .\\n?___29 skos:prefLabel ?___30 .\\n?item def-bw:regionalOrganization ?___31 .\\n?___31 skos:prefLabel ?___32 .\\n?item def-ef:samplingPoint ?___33 .\\n?___33 geo:long ?___34 .\\n?___33 spatialrelations:easting ?___35 .\\n?___33 geo:lat ?___36 .\\n?___33 spatialrelations:northing ?___37 .\\n?___33 skos:prefLabel ?___38 .\\n?item skos:prefLabel ?___39 .\\n?item owl:sameAs ?___40 .\\n?item def-bwp:latestBathingWaterProfile ?___41 .\\n?item def-zoi:zoneOfInfluence ?___42 .\\n?___42 skos:prefLabel ?___43 .\\n\\n} WHERE {\\n  {SELECT DISTINCT ?item\\n    WHERE {\\n    ?item def-ef:samplingPoint ?___samplingPoint_0 .\\n    ?item rdf:type def-bw:BathingWater .\\n    OPTIONAL { ?item def-bw:yearDedesignated ?___1 . }\\n    OPTIONAL { ?___samplingPoint_0 def-ef:samplePointNotation ?___2 . }\\n     FILTER (!(bound(?___1)))\\n    }  ORDER BY  ?___2  ?item OFFSET 0 LIMIT 10\\n}{{ ?item def-bwq:latestComplianceAssessment ?___3 . }  OPTIONAL {\\n  {{ ?___3 def-bwq:complianceClassification ?___4 . }  OPTIONAL {\\n    { ?___4 skos:prefLabel ?___5 . } \\n  }}\\n}}\\nUNION { ?item rdfs:seeAlso ?___6 . } \\nUNION { ?item def-bw:sedimentTypesPresent ?___7 . } \\nUNION { ?item def-bw:yearDesignated ?___8 . } \\nUNION { ?item def-bw:eubwidNotation ?___9 . } \\nUNION {{ ?item def-geom:envelope ?___10 . }  OPTIONAL {\\n  { ?___10 rdfs:label ?___11 . } \\n}}\\nUNION { ?item def-bw:yearDedesignated ?___12 . } \\nUNION {{ ?item onsadmingeo:county ?___13 . }  OPTIONAL {\\n  { ?___13 skos:prefLabel ?___14 . } \\n}}\\nUNION {{ ?item def-bwq:latestSampleAssessment ?___15 . }  OPTIONAL {\\n  {{ ?___15 def-bwq:sampleClassification ?___16 . }  OPTIONAL {\\n    { ?___16 skos:prefLabel ?___17 . } \\n  }}\\n}}\\nUNION {{ ?item dgu:uriSet ?___18 . }  OPTIONAL {\\n  { ?___18 rdfs:label ?___19 . } \\n  UNION { ?___18 skos:prefLabel ?___20 . } \\n}}\\nUNION { ?item def-bw:waterQualityImpactedByHeavyRain ?___21 . } \\nUNION {{ ?item def-stp:latestRiskPrediction ?___22 . }  OPTIONAL {\\n  {{ ?___22 def-stp:riskLevel ?___23 . }  OPTIONAL {\\n    { ?___23 skos:prefLabel ?___24 . } \\n  }}\\n  UNION { ?___22 def-stp:expiresAt ?___25 . } \\n}}\\nUNION {{ ?item onsadmingeo:district ?___26 . }  OPTIONAL {\\n  { ?___26 skos:prefLabel ?___27 . } \\n}}\\nUNION { ?item rdf:type ?___28 . } \\nUNION {{ ?item onsadmingeo:country ?___29 . }  OPTIONAL {\\n  { ?___29 skos:prefLabel ?___30 . } \\n}}\\nUNION {{ ?item def-bw:regionalOrganization ?___31 . }  OPTIONAL {\\n  { ?___31 skos:prefLabel ?___32 . } \\n}}\\nUNION {{ ?item def-ef:samplingPoint ?___33 . }  OPTIONAL {\\n  { ?___33 geo:long ?___34 . } \\n  UNION { ?___33 spatialrelations:easting ?___35 . } \\n  UNION { ?___33 geo:lat ?___36 . } \\n  UNION { ?___33 spatialrelations:northing ?___37 . } \\n  UNION { ?___33 skos:prefLabel ?___38 . } \\n}}\\nUNION { ?item skos:prefLabel ?___39 . } \\nUNION { ?item owl:sameAs ?___40 . } \\nUNION { ?item def-bwp:latestBathingWaterProfile ?___41 . } \\nUNION {{ ?item def-zoi:zoneOfInfluence ?___42 . }  OPTIONAL {\\n  { ?___42 skos:prefLabel ?___43 . } \\n}}\\n\\n}\" ]\n" +
            "                          ]\n" +
            "                ] ;\n" +
            "        <http://www.w3.org/1999/xhtml/vocab#first>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all&_page=0> ;\n" +
            "        <http://www.w3.org/1999/xhtml/vocab#next>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all&_page=1> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.xml?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"xml\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"application/xml\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.rdf?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"rdf\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"application/rdf+xml\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.html?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"html\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"text/html\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.csv?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"csv\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"text/csv\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water?_view=basic&_metadata=all&_page=0>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"basic\" ;\n" +
            "        <http://purl.org/dc/terms/isVersionOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#properties>\n" +
            "                \"type\" , \"label\" ;\n" +
            "        <http://www.epimorphics.com/vocabularies/lda#viewName>\n" +
            "                \"basic\" .\n" +
            "\n" +
            "<https://github.com/epimorphics/elda.git>\n" +
            "        a       <http://usefulinc.com/ns/doap#Repository> ;\n" +
            "        <http://usefulinc.com/ns/doap#browse>\n" +
            "                <https://github.com/epimorphics/elda> ;\n" +
            "        <http://usefulinc.com/ns/doap#location>\n" +
            "                <https://github.com/epimorphics/elda> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water?_view=description&_metadata=all&_page=0>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"description\" ;\n" +
            "        <http://purl.org/dc/terms/isVersionOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> ;\n" +
            "        <http://www.epimorphics.com/vocabularies/lda#viewName>\n" +
            "                \"description\" .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.text?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"text\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"text/plain\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/bwq/doc/bathing-water.ttl?_metadata=all>\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"ttl\" ;\n" +
            "        <http://purl.org/dc/terms/format>\n" +
            "                [ <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                          \"text/turtle\" ] ;\n" +
            "        <http://purl.org/dc/terms/isFormatOf>\n" +
            "                <http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all> .\n" +
            "\n" +
            "_:b0    a       <http://www.w3.org/ns/sparql-service-description#Service> ;\n" +
            "        <http://purl.org/linked-data/api/vocab#sparqlEndpoint>\n" +
            "                <http://environment.data.gov.uk/sparql/bwq/query> ;\n" +
            "        <http://www.w3.org/ns/sparql-service-description#url>\n" +
            "                <http://environment.data.gov.uk/sparql/bwq/query> .";

    public static final String PAGE_OBJECT_GAMES = "@prefix hello: <http://epimorphics.com/public/vocabulary/games.ttl#> .\n" +
            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix elda:  <http://www.epimorphics.com/vocabularies/lda#> .\n" +
            "@prefix dct:   <http://purl.org/dc/terms/> .\n" +
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix api:   <http://purl.org/linked-data/api/vocab#> .\n" +
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "\n" +
            "<http://www.treefroggames.com/a-few-acres-of-snow-2>\n" +
            "        a                      hello:BoardGame ;\n" +
            "        rdfs:label             \"A Few Acres of Snow\" ;\n" +
            "        hello:designed-by      <http://boardgamegeek.com/boardgamedesigner/6/martin-wallace> ;\n" +
            "        hello:playTimeMinutes  60 ;\n" +
            "        hello:players          2 ;\n" +
            "        hello:pubYear          2011 .\n" +
            "\n" +
            "<http://www.hans-im-glueck.de/el-grande>\n" +
            "        a                  hello:BoardGame ;\n" +
            "        rdfs:label         \"El Grande\" ;\n" +
            "        hello:designed-by  <http://www.boardgamegeek.com/boardgamedesigner/8/richard-ulrich> , <http://en.wikipedia.org/wiki/Wolfgang_Kramer> ;\n" +
            "        hello:players      5 , 4 , 3 , 2 ;\n" +
            "        hello:pubYear      1995 .\n" +
            "\n" +
            "<http://www.aleaspiele.de/Pages/C6/>\n" +
            "        a                      hello:BoardGame ;\n" +
            "        rdfs:label             \"Glen More\" ;\n" +
            "        hello:designed-by      <http://boardgamegeek.com/boardgamedesigner/34699/matthias-cramer> ;\n" +
            "        hello:playTimeMinutes  60 ;\n" +
            "        hello:players          4 , 3 , 2 , 5 ;\n" +
            "        hello:pubYear          2010 ;\n" +
            "        hello:published-by     <http://www.aleaspiele.de/Pages/Title/> .\n" +
            "\n" +
            "<http://www.ragnarbrothers.co.uk/html/canal_mania.html>\n" +
            "        a                  hello:BoardGame ;\n" +
            "        rdfs:label         \"Canal Mania\" ;\n" +
            "        hello:designed-by  <http://www.ragnarbrothers.co.uk/html/ragnar_brothers_games.html> ;\n" +
            "        hello:players      5 , 4 , 3 , 2 ;\n" +
            "        hello:pubYear      2006 .\n" +
            "\n" +
            "<http://www.treefroggames.com/age-of-industry>\n" +
            "        a                  hello:BoardGame ;\n" +
            "        rdfs:label         \"Age of Industry\" ;\n" +
            "        hello:designed-by  <http://boardgamegeek.com/boardgamedesigner/6/martin-wallace> ;\n" +
            "        hello:players      5 , 4 , 3 , 2 ;\n" +
            "        hello:pubYear      2010 .\n" +
            "\n" +
            "<http://www.treefroggames.com/age-of-steam>\n" +
            "        a                  hello:BoardGame ;\n" +
            "        rdfs:label         \"Age of Steam\" ;\n" +
            "        hello:designed-by  <http://boardgamegeek.com/boardgamedesigner/6/martin-wallace> ;\n" +
            "        hello:players      6 , 5 , 4 , 3 ;\n" +
            "        hello:pubYear      2002 .\n" +
            "\n" +
            "<http://www.treefroggames.com/last-train-to-wensleydale>\n" +
            "        a                      hello:BoardGame ;\n" +
            "        rdfs:label             \"Last Train to Wensleydale\" ;\n" +
            "        hello:designed-by      <http://boardgamegeek.com/boardgamedesigner/6/martin-wallace> ;\n" +
            "        hello:playTimeMinutes  120 ;\n" +
            "        hello:players          4 , 3 ;\n" +
            "        hello:pubYear          2009 .\n" +
            "\n" +
            "<http://queen-games.de/games.aspx?ProductId=45>\n" +
            "        a                  hello:BoardGame ;\n" +
            "        rdfs:label         \"Kingdom Builder\" ;\n" +
            "        rdfs:seeAlso       <http://boardgamegeek.com/boardgame/107529/kingdom-builder> ;\n" +
            "        hello:designed-by  <http://boardgamegeek.com/boardgamedesigner/10525/donald-x-vaccarino> ;\n" +
            "        hello:players      4 , 3 , 2 ;\n" +
            "        hello:pubYear      2011 .\n" +
            "\n" +
            "<http://boardgamegeek.com/boardgame/96913/lancaster>\n" +
            "        a                      hello:BoardGame ;\n" +
            "        rdfs:label             \"Lancaster\" ;\n" +
            "        hello:designed-by      <http://boardgamegeek.com/boardgamedesigner/34699/matthias-cramer> ;\n" +
            "        hello:playTimeMinutes  60 ;\n" +
            "        hello:players          2 , 3 , 4 , 5 ;\n" +
            "        hello:pubYear          2011 ;\n" +
            "        hello:published-by     <http://www.queen-games.de/> .\n" +
            "\n" +
            "<http://www.ragnarbrothers.co.uk/html/brief_history_of_the_world1.html>\n" +
            "        a                  hello:BoardGame ;\n" +
            "        rdfs:label         \"A Brief History of the World\" ;\n" +
            "        hello:designed-by  <http://www.ragnarbrothers.co.uk/html/ragnar_brothers_games.html> ;\n" +
            "        hello:players      6 , 5 , 4 , 3 ;\n" +
            "        hello:pubYear      2009 .\n" +
            "";

    public static final String PAGE_METADATA_GAMES = "@prefix hello: <http://epimorphics.com/public/vocabulary/games.ttl#> .\n" +
            "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@prefix elda:  <http://www.epimorphics.com/vocabularies/lda#> .\n" +
            "@prefix dct:   <http://purl.org/dc/terms/> .\n" +
            "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix api:   <http://purl.org/linked-data/api/vocab#> .\n" +
            "@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.rdf?_metadata=all>\n" +
            "        rdfs:label      \"rdf\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/rdf+xml\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.xml?_metadata=all>\n" +
            "        rdfs:label      \"xml\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/xml\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.vhtml?_view=description&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"description\" ;\n" +
            "        dct:isVersionOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        elda:viewName    \"description\" .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.html?_metadata=all>\n" +
            "        rdfs:label      \"html\" ;\n" +
            "        dct:format      [ rdfs:label  \"text/html\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.json?_metadata=all>\n" +
            "        rdfs:label      \"json\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/json\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.vhtml?_view=basic&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"basic\" ;\n" +
            "        dct:isVersionOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        api:properties   \"type\" , \"label\" ;\n" +
            "        elda:viewName    \"basic\" .\n" +
            "\n" +
            "<https://elda.googlecode.com/hg/>\n" +
            "        a       <http://usefulinc.com/ns/doap#Repository> ;\n" +
            "        <http://usefulinc.com/ns/doap#browse>\n" +
            "                <http://code.google.com/p/elda/source/browse/> ;\n" +
            "        <http://usefulinc.com/ns/doap#location>\n" +
            "                <https://elda.googlecode.com> .\n" +
            "\n" +
            "elda:Elda_1.2.35-SNAPSHOT\n" +
            "        a           <http://usefulinc.com/ns/doap#Version> ;\n" +
            "        rdfs:label  \"Elda 1.2.35-SNAPSHOT\" ;\n" +
            "        <http://usefulinc.com/ns/doap#releaseOf>\n" +
            "                [ rdfs:label  \"Elda\" ;\n" +
            "                  <http://usefulinc.com/ns/doap#bug-database>\n" +
            "                          <http://code.google.com/p/elda/issues/list> ;\n" +
            "                  <http://usefulinc.com/ns/doap#homepage>\n" +
            "                          <http://elda.googlecode.com> ;\n" +
            "                  <http://usefulinc.com/ns/doap#implements>\n" +
            "                          \"http://code.google.com/p/linked-data-api/wiki/Specification\" ;\n" +
            "                  <http://usefulinc.com/ns/doap#programming-language>\n" +
            "                          \"Java\" ;\n" +
            "                  <http://usefulinc.com/ns/doap#repository>\n" +
            "                          <https://elda.googlecode.com/hg/> ;\n" +
            "                  <http://usefulinc.com/ns/doap#wiki>\n" +
            "                          <http://code.google.com/p/elda/w/list>\n" +
            "                ] ;\n" +
            "        <http://usefulinc.com/ns/doap#revision>\n" +
            "                \"1.2.35-SNAPSHOT\" .\n" +
            "\n" +
            "_:b0    a                   <http://www.w3.org/ns/sparql-service-description#Service> ;\n" +
            "        api:sparqlEndpoint  <local:data/example-data.ttl> ;\n" +
            "        <http://www.w3.org/ns/sparql-service-description#url>\n" +
            "                <local:data/example-data.ttl> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.atom?_metadata=all>\n" +
            "        rdfs:label      \"atom\" ;\n" +
            "        dct:format      [ rdfs:label  \"application/atom+xml\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.text?_metadata=all>\n" +
            "        rdfs:label      \"text\" ;\n" +
            "        dct:format      [ rdfs:label  \"text/plain\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.ttl?_metadata=all>\n" +
            "        rdfs:label      \"ttl\" ;\n" +
            "        dct:format      [ rdfs:label  \"text/turtle\" ] ;\n" +
            "        dct:isFormatOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.vhtml?_metadata=all>\n" +
            "        a                            api:Page , api:ListEndpoint ;\n" +
            "        rdfs:label                   \"vhtml\" ;\n" +
            "        <http://a9.com/-/spec/opensearch/1.1/itemsPerPage>\n" +
            "                \"10\"^^xsd:long ;\n" +
            "        <http://a9.com/-/spec/opensearch/1.1/startIndex>\n" +
            "                \"1\"^^xsd:long ;\n" +
            "        dct:format                   [ rdfs:label\n" +
            "                          \"text/html\" ] ;\n" +
            "        dct:hasFormat                <http://localhost:8080/standalone/hello/games.ttl?_metadata=all> , <http://localhost:8080/standalone/hello/games.rdf?_metadata=all> , <http://localhost:8080/standalone/hello/games.html?_metadata=all> , <http://localhost:8080/standalone/hello/games.atom?_metadata=all> , <http://localhost:8080/standalone/hello/games.text?_metadata=all> , <http://localhost:8080/standalone/hello/games.json?_metadata=all> , <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> , <http://localhost:8080/standalone/hello/games.xml?_metadata=all> ;\n" +
            "        dct:hasPart                  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        dct:hasVersion               <http://localhost:8080/standalone/hello/games.vhtml?_view=basic&_metadata=all&_page=0> , <http://localhost:8080/standalone/hello/games.vhtml?_view=all&_metadata=all&_page=0> , <http://localhost:8080/standalone/hello/games.vhtml?_view=description&_metadata=all&_page=0> ;\n" +
            "        dct:isFormatOf               <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        dct:isPartOf                 <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        api:definition               <http://localhost:8080/standalone/hello/meta/games.vhtml?_metadata=all> ;\n" +
            "        api:extendedMetadataVersion  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        api:items                    ( <http://www.ragnarbrothers.co.uk/html/brief_history_of_the_world1.html> <http://www.treefroggames.com/a-few-acres-of-snow-2> <http://www.treefroggames.com/age-of-industry> <http://www.treefroggames.com/age-of-steam> <http://www.ragnarbrothers.co.uk/html/canal_mania.html> <http://www.hans-im-glueck.de/el-grande> <http://www.aleaspiele.de/Pages/C6/> <http://queen-games.de/games.aspx?ProductId=45> <http://boardgamegeek.com/boardgame/96913/lancaster> <http://www.treefroggames.com/last-train-to-wensleydale> ) ;\n" +
            "        api:page                     \"0\"^^xsd:long ;\n" +
            "        api:wasResultOf              [ a                    api:Execution ;\n" +
            "                                       api:processor        [ a       api:Service ;\n" +
            "                                                              <http://purl.org/net/opmv/types/common#software>\n" +
            "                                                                      elda:Elda_1.2.35-SNAPSHOT\n" +
            "                                                            ] ;\n" +
            "                                       api:selectionResult  [ a       <http://purl.org/net/opmv/types/sparql#QueryResult> ;\n" +
            "                                                              <http://purl.org/net/opmv/types/sparql#endpoint>\n" +
            "                                                                      _:b0 ;\n" +
            "                                                              <http://purl.org/net/opmv/types/sparql#query>\n" +
            "                                                                      [ rdf:value  \"PREFIX hello: <http://epimorphics.com/public/vocabulary/games.ttl#>\\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\\nPREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\\nSELECT DISTINCT ?item\\nWHERE {\\n?item rdf:type hello:BoardGame .\\nOPTIONAL { ?item rdfs:label ?___0 . }\\n}  ORDER BY  ?___0  ?item OFFSET 0 LIMIT 10\" ]\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"BoardGame\" ;\n" +
            "                                                              api:property  hello:BoardGame\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"isPartOf\" ;\n" +
            "                                                              api:property  dct:isPartOf\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"label\" ;\n" +
            "                                                              api:property  rdfs:label\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"type\" ;\n" +
            "                                                              api:property  rdf:type\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"playTimeMinutes\" ;\n" +
            "                                                              api:property  hello:playTimeMinutes\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"page\" ;\n" +
            "                                                              api:property  api:page\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"definition\" ;\n" +
            "                                                              api:property  api:definition\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"itemsPerPage\" ;\n" +
            "                                                              api:property  <http://a9.com/-/spec/opensearch/1.1/itemsPerPage>\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"first\" ;\n" +
            "                                                              api:property  <http://www.w3.org/1999/xhtml/vocab#first>\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"publicationYear\" ;\n" +
            "                                                              api:property  hello:pubYear\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"players\" ;\n" +
            "                                                              api:property  hello:players\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"extendedMetadataVersion\" ;\n" +
            "                                                              api:property  api:extendedMetadataVersion\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"startIndex\" ;\n" +
            "                                                              api:property  <http://a9.com/-/spec/opensearch/1.1/startIndex>\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"next\" ;\n" +
            "                                                              api:property  <http://www.w3.org/1999/xhtml/vocab#next>\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"hasPart\" ;\n" +
            "                                                              api:property  dct:hasPart\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"designedBy\" ;\n" +
            "                                                              api:property  hello:designed-by\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"items\" ;\n" +
            "                                                              api:property  api:items\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_HOST\" ;\n" +
            "                                                              api:value  \"localhost:8080\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_properties\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_view\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_APP\" ;\n" +
            "                                                              api:value  \"/standalone\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_metadata\" ;\n" +
            "                                                              api:value  \"all\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_rootPath\" ;\n" +
            "                                                              api:value  \"/standalone/hello\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_page\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_suffix\" ;\n" +
            "                                                              api:value  \"vhtml\"\n" +
            "                                                            ] ;\n" +
            "                                       api:viewingResult    [ a       <http://purl.org/net/opmv/types/sparql#QueryResult> ;\n" +
            "                                                              <http://purl.org/net/opmv/types/sparql#endpoint>\n" +
            "                                                                      _:b0 ;\n" +
            "                                                              <http://purl.org/net/opmv/types/sparql#query>\n" +
            "                                                                      [ rdf:value  \"DESCRIBE \\n  <http://www.ragnarbrothers.co.uk/html/canal_mania.html>\\n  <http://queen-games.de/games.aspx?ProductId=45>\\n  <http://www.treefroggames.com/a-few-acres-of-snow-2>\\n  <http://www.treefroggames.com/age-of-industry>\\n  <http://www.ragnarbrothers.co.uk/html/brief_history_of_the_world1.html>\\n  <http://www.aleaspiele.de/Pages/C6/>\\n  <http://www.treefroggames.com/last-train-to-wensleydale>\\n  <http://www.treefroggames.com/age-of-steam>\\n  <http://boardgamegeek.com/boardgame/96913/lancaster>\\n  <http://www.hans-im-glueck.de/el-grande>\" ]\n" +
            "                                                            ]\n" +
            "                                     ] ;\n" +
            "        <http://www.w3.org/1999/xhtml/vocab#first>\n" +
            "                <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all&_page=0> ;\n" +
            "        <http://www.w3.org/1999/xhtml/vocab#next>\n" +
            "                <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all&_page=1> .\n" +
            "\n" +
            "<http://localhost:8080/standalone/hello/games.vhtml?_view=all&_metadata=all&_page=0>\n" +
            "        rdfs:label       \"all\" ;\n" +
            "        dct:isVersionOf  <http://localhost:8080/standalone/hello/games.vhtml?_metadata=all> ;\n" +
            "        elda:viewName    \"all\" .\n" +
            "";



    /***********************************/
    /* Static variables                */
    /***********************************/
    public static final Model shortNameServiceFixtureModel = ModelIOUtils.modelFromTurtle
            ( "@prefix : <http://example/test/>. "
                    + "<stub:root> a api:API. "
                    + ":p a rdf:Property; api:label 'name_p'. "
                    + ":q a rdf:Property; api:label 'name_q'; rdfs:range xsd:decimal."
                    );


    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Create an APIResultSet fixture without trying to do all that that very complex
     * class does.
     * @return Mocked {@link APIResultSet}
     */
    public static APIResultSet mockResultSet( JUnitRuleMockery context,
                                              final Model apiResultsModel,
                                              final Model apiObjectModel,
                                              final Model apiMetadataModel ) {
        return mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel, "APIResultSet" );
    }
    
    /**
     * Create a named APIResultSet fixture without trying to do all that that very complex
     * class does.
     * @return Mocked {@link APIResultSet}
     */
    public static APIResultSet mockResultSet( JUnitRuleMockery context,
                                              final Model apiResultsModel,
                                              final Model apiObjectModel,
                                              final Model apiMetadataModel,
                                              String mockName ) {
        final APIResultSet results = context.mock( APIResultSet.class, mockName );
        final APIResultSet.MergedModels mm = context.mock( APIResultSet.MergedModels.class, mockName + "-mm" );
        final Resource root = apiResultsModel.listResourcesWithProperty( RDF.type, API.Page ).next();

        context.checking(new Expectations() {{
            atLeast(0).of (results).getModels();
            will( returnValue( mm ) );

            atLeast(0).of (mm).getMergedModel();
            will( returnValue( apiResultsModel ));

            atLeast(0).of (mm).getObjectModel();
            will( returnValue( apiObjectModel));

            atLeast(0).of (mm).getMetaModel();
            will( returnValue( apiMetadataModel ));

            atLeast(0).of (results).getRoot();
            will( returnValue( root ) );
        }});

        return results;
    }

    /**
     * @return Stub {@link ShortnameService}
     */
    public static ShortnameService shortNameServiceFixture() {
        Resource root = shortNameServiceFixtureModel.createResource( "stub:root" );
        return new StandardShortnameService( root, shortNameServiceFixtureModel, null );
    }




    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

