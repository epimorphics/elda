/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;



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
            "@prefix doap:  <http://usefulinc.com/ns/doap#> .";

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

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

