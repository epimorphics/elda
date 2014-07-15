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

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
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
            "";

    /** A page of bathing water results */
    public static final String PAGE_BWQ =
            "<http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy>\n" +
            "        a                            api:Page ;\n" +
            "        rdfs:label                   \"ttl\" ;\n" +
            "        os:itemsPerPage              \"10\"^^xsd:long ;\n" +
            "        os:startIndex                \"1\"^^xsd:long ;\n" +
            "        dct:format                   [ rdfs:label\n" +
            "                          \"text/turtle\" ] ;\n" +
            "        dct:hasFormat                <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.text?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.rdf?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.csv?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.xml?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.html?_view=all&_metadata=all&_page=0&_lang=en,cy> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.json?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        dct:hasVersion               <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=assessment-csv&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=description&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=basic&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=streptococci&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=coliforms&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=location&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=assessment&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=salmonella&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=classification&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=entrovirus&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=escherichiaColi&_metadata=all&_page=0> , <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=intestinalEnterococci&_metadata=all&_page=0> ;\n" +
            "        dct:isFormatOf               <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy> ;\n" +
            "        dct:isPartOf                 <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all> ;\n" +
            "        api:definition               <http://environment.data.gov.uk/meta/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all> ;\n" +
            "        api:extendedMetadataVersion  <http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=all&_metadata=all&_page=0> ;\n" +
            "        api:items                    ( <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03600/date/20140702/time/100000/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03700/date/20140702/time/104200/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03800/date/20140702/time/105800/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03900/date/20140702/time/112500/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04000/date/20140702/time/115500/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04200/date/20140702/time/122000/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04250/date/20140702/time/115000/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04280/date/20140702/time/112000/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04300/date/20140702/time/105500/recordDate/20140702> <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04400/date/20140702/time/103500/recordDate/20140702> ) ;\n" +
            "        api:page                     \"0\"^^xsd:long ;\n" +
            "        api:wasResultOf              [ a                    api:Execution ;\n" +
            "                                       api:processor        [ a              api:Service ;\n" +
            "                                                              opmv:software  elda:Elda_1.2.33\n" +
            "                                                            ] ;\n" +
            "                                       api:selectionResult  [ a                sparql:QueryResult ;\n" +
            "                                                              sparql:endpoint  _:b0 ;\n" +
            "                                                              sparql:query     [ rdf:value  \"PREFIX bwq-iss: <http://environment.data.gov.uk/data/bathing-water-quality/in-season/slice/>\\nPREFIX def-bwq: <http://environment.data.gov.uk/def/bathing-water-quality/>\\nPREFIX def-ef: <http://location.data.gov.uk/def/ef/SamplingPoint/>\\nPREFIX qb: <http://purl.org/linked-data/cube#>\\nSELECT DISTINCT ?item\\nWHERE {\\nbwq-iss:latest qb:observation ?item.OPTIONAL { ?item def-bwq:samplingPoint ?___1 . ?___1 def-ef:samplePointNotation ?___0 . }\\n}  ORDER BY  ?___0  ?item OFFSET 0 LIMIT 10\" ]\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"abnormalWeatherException\" ;\n" +
            "                                                              api:property  def-bwq:abnormalWeatherException\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"new\" ;\n" +
            "                                                              api:property  def-bwq:new\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"recordStatus\" ;\n" +
            "                                                              api:property  def-bwq:recordStatus\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleYear\" ;\n" +
            "                                                              api:property  def-bwq:sampleYear\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"bwq_samplingPoint\" ;\n" +
            "                                                              api:property  def-bwq:samplingPoint\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleClassification\" ;\n" +
            "                                                              api:property  def-bwq:sampleClassification\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"source\" ;\n" +
            "                                                              api:property  dct:source\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"items\" ;\n" +
            "                                                              api:property  api:items\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"extendedMetadataVersion\" ;\n" +
            "                                                              api:property  api:extendedMetadataVersion\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"Observation\" ;\n" +
            "                                                              api:property  qb:Observation\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"escherichiaColiQualifier\" ;\n" +
            "                                                              api:property  def-bwq:escherichiaColiQualifier\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"startIndex\" ;\n" +
            "                                                              api:property  os:startIndex\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"itemsPerPage\" ;\n" +
            "                                                              api:property  os:itemsPerPage\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"SampleAssessment\" ;\n" +
            "                                                              api:property  def-bwq:SampleAssessment\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"escherichiaColiCount\" ;\n" +
            "                                                              api:property  def-bwq:escherichiaColiCount\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"bwq_bathingWater\" ;\n" +
            "                                                              api:property  def-bwq:bathingWater\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleDateTime\" ;\n" +
            "                                                              api:property  def-bwq:sampleDateTime\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"dataset\" ;\n" +
            "                                                              api:property  qb:dataSet\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"first\" ;\n" +
            "                                                              api:property  xhv:first\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"label\" ;\n" +
            "                                                              api:property  rdfs:label\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"page\" ;\n" +
            "                                                              api:property  api:page\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"intestinalEnterococciCount\" ;\n" +
            "                                                              api:property  def-bwq:intestinalEnterococciCount\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"intestinalEnterococciQualifier\" ;\n" +
            "                                                              api:property  def-bwq:intestinalEnterococciQualifier\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"created\" ;\n" +
            "                                                              api:property  dct:created\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"isPartOf\" ;\n" +
            "                                                              api:property  dct:isPartOf\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"recordDate\" ;\n" +
            "                                                              api:property  def-bwq:recordDate\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"hasPart\" ;\n" +
            "                                                              api:property  dct:hasPart\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"api_definition\" ;\n" +
            "                                                              api:property  api:definition\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"sampleWeek\" ;\n" +
            "                                                              api:property  def-bwq:sampleWeek\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"type\" ;\n" +
            "                                                              api:property  rdf:type\n" +
            "                                                            ] ;\n" +
            "                                       api:termBinding      [ api:label     \"next\" ;\n" +
            "                                                              api:property  xhv:next\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_rootPath\" ;\n" +
            "                                                              api:value  \"null\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_APP\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_suffix\" ;\n" +
            "                                                              api:value  \"ttl\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_properties\" ;\n" +
            "                                                              api:value  \"\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_view\" ;\n" +
            "                                                              api:value  \"all\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"sp\" ;\n" +
            "                                                              api:value  \"http://location.data.gov.uk/so/ef/SamplingPoint/bwsp.eaew/{bwspid}\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"bw\" ;\n" +
            "                                                              api:value  \"http://environment.data.gov.uk/id/bathing-water/{eubwid}\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_metadata\" ;\n" +
            "                                                              api:value  \"all\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_HOST\" ;\n" +
            "                                                              api:value  \"environment.data.gov.uk\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"visibleSparqlEndpoint\" ;\n" +
            "                                                              api:value  \"http://environment.data.gov.uk/sparql/bwq/query\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_resourceRoot\" ;\n" +
            "                                                              api:value  \"/lda-assets/\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_strip_has\" ;\n" +
            "                                                              api:value  \"yes\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_page\" ;\n" +
            "                                                              api:value  \"0\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"visibleSparqlForm\" ;\n" +
            "                                                              api:value  \"http://environment.data.gov.uk/lab/sparql.html\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_suppress_ipto\" ;\n" +
            "                                                              api:value  \"yes\"\n" +
            "                                                            ] ;\n" +
            "                                       api:variableBinding  [ api:label  \"_lang\" ;\n" +
            "                                                              api:value  \"en,cy\"\n" +
            "                                                            ] ;\n" +
            "                                       api:viewingResult    [ a                sparql:QueryResult ;\n" +
            "                                                              sparql:endpoint  _:b0 ;\n" +
            "                                                              sparql:query     [ rdf:value  \"DESCRIBE\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04400/date/20140702/time/103500/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04200/date/20140702/time/122000/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04300/date/20140702/time/105500/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04280/date/20140702/time/112000/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04000/date/20140702/time/115500/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03800/date/20140702/time/105800/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03900/date/20140702/time/112500/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03600/date/20140702/time/100000/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03700/date/20140702/time/104200/recordDate/20140702>\\n  <http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/04250/date/20140702/time/115000/recordDate/20140702>\" ]\n" +
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
            "_:b0    a                   <http://www.w3.org/ns/sparql-service-description#Service> ;\n" +
            "        api:sparqlEndpoint  <http://localhost:3030/bwq/query> ;\n" +
            "        <http://www.w3.org/ns/sparql-service-description#url>\n" +
            "                <http://localhost:3030/bwq/query> .\n" +
            "";

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
        final APIResultSet results = context.mock( APIResultSet.class );
        final APIResultSet.MergedModels mm = context.mock( APIResultSet.MergedModels.class );
        final Resource root = apiResultsModel.listResourcesWithProperty( RDF.type, API.Page ).next();

        context.checking(new Expectations() {{
            atLeast(1).of (results).getModels();
            will( returnValue( mm ) );

            atLeast(1).of (mm).getMergedModel();
            will( returnValue( apiResultsModel ));

            atLeast(1).of (mm).getObjectModel();
            will( returnValue( apiObjectModel));

            atLeast(1).of (mm).getMetaModel();
            will( returnValue( apiMetadataModel ));

            atLeast(1).of (results).getRoot();
            will( returnValue( root ) );
        }});

        return results;
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

