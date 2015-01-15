---
title: Elda 1.3.4
layout: default-toc
---

Introduction
============

This document is an introduction to Elda, an implementation of the
[Linked Data API
(LDA)](http://code.google.com/p/linked-data-api/wiki/Specification). The
LDA allows you to have a configurable, REST-style interface to a store
containing [RDF](http://www.w3.org/RDF/) data. This makes it easier for
developers used to using common web technologies such as JavaScript and
JSON to access your data, and display it in a web browser.

Elda is a Java implementation of the LDA specification, and was
developed by [Epimorphics Ltd](http://www.epimorphics.com). Elda is
[licensed](https://github.com/epimorphics/elda/LICENCE.html) under an
open-source [Apache
License](http://www.apache.org/licenses/LICENSE-2.0).

A quick introduction to running Elda
------------------------------------

Before we go on to explain more of what Elda is doing and how you can
customise it, let's get an instance of Elda running and see what it can
do.

The easiest route to starting Elda is to use the pre-packaged Java
`.jar` file that you can download from
<http://repository.epimorphics.com/com/epimorphics/lda/elda-standalone/{{ site.data.version.CURRENT_RELEASE }}/elda-standalone-{{ site.data.version.CURRENT_RELEASE }}-exec-war.jar>.
You can start the file from the command line.

    java -jar elda-standalone-$VERSION-exec-war.jar

where *`$VERSION`* denotes the version number of the Elda you
downloaded, such as {{ site.data.version.CURRENT_RELEASE }}. If your system permits, double-clicking on
the file in your preferred file browser may also work.

At this point, you may see a large number of log messages as Elda
starts, and then you should be able to view the starting page in a web
browser: <http://localhost:8080/standalone/again/games>. This should
display a page similar to:

![Screenshot showing demo dataset in
Elda](elda-demo-screenshot-2.png "Screenshot showing demo dataset")

### What just happened?

Contained within the standalone Elda version is a small RDF dataset
about board games. The [example
configuration](https://github.com/epimorphics/elda/elda-standalone/src/main/webapp/specs/hello-world.ttl)
provides an API for viewing the contents of this dataset through simple
web URLs. `http://localhost:8080/standalone/hello/games` lists the first
page of board games that are in the example data.

By default, the list is presented in HTML format, which is nice for
people to read, but not so good for programs to process. The
*data format* pulldown on the top-right of the page are clickable to 
select a different format, *eg* by clicking the
[ttl](http://localhost:8080/standalone/hello/games.ttl) link, the
BoardGame resources will be shown as RDF Turtle.

The pull-down menus also allow you to *go to* a selected item,
to adjust the *page* size and which page to view, or to change the
*view* (which properties to show) of the data.

### How did it happen?

When Elda is presented with a URL, it uses it to select an *endpoint*.
From the details of the URL path (`/games` in this case) and its query
parameters (none in this case), it constructs a *SPARQL query* which
selects one or more *items* (here, games) from the RDF dataset. It then
constructs a *view* of (some of the) properties of those items, and then
*renders* that view into one of the possible formats, sending that
rendering back to whatever sent the query.

This example displays all available properties.

Decisions about which datasets to display, which end-points ( *ie*, URL
patterns) display which resources, how resources are displayed, and
other design choices are all encoded in the LDA configuration file. The
[LDA
specification](http://code.google.com/p/linked-data-api/wiki/Specification)
describes in detail what goes in a configuration file. The configuration
file itself uses RDF to encode these configuration choices, and is
typically written in the
[Turtle](http://en.wikipedia.org/wiki/Turtle_(syntax)) syntax, as it is
relatively readable and compact. Elda comes with various pre-built
examples which may help you to get started with building your own specs.
Ultimately, it is the configuration file which specifies how Elda URLs
are turned into queries against the underlying store, and how the
results of those queries are presented back to your users.

The [cribsheet](cribsheet.html) gives a terse overview of the meaning of
the LDA query parameters and configuration properties.

Summary of LDA capabilities
---------------------------

-   *filters*. Query parameters can specify that items must have
    specified ranges of values for some property in order to be
    selected. Endpoints can have some filters, *e.g.* that the item has
    a particular type, pre-loaded.
-   *views*. Endpoints can specify, and query parameters modify, which
    properties of the selected items are to be displayed.
-   *formats*. The items and their properties can be rendered in
    different formats, including HTML, XML, JSON, RDF/XML, and Turtle.
-   *data sources*. Data can be fetched from any SPARQL endpoint. For
    convenience in testing, Elda also allows data to be fetched from a
    local file.
-   *metadata*. The rendered response includes metadata such as the
    SPARQL queries used to select items and fetch their properties.
-   *text search*. For suitably-configured SPARQL endpoints, Elda can
    exploit *free text queries* that can search for text fragments or
    use AND, OR, and NOT to control the search.
-   *velocity templates*. You can choose to write a new format for an
    Elda instance using Velocity templates. (This feature is
    experimental; feedback is welcome.)
-   *statistics*. Elda can present statistics about the queries that it
    has received either in HTML or (experimentally) using JMX. The
    statistics include the number of queries and the amount of data
    transferred.
-   *configuration display*. The path `/api-config` for a running Elda
    presents all the endpoints that are available for it and details of
    their configuration.

Tutorial
========

We will now work through some of Elda's capabilities in slightly more
detail. See the [reference page](reference.html) for more comprehensive
and detailed descriptions.

Prerequisites
-------------

Elda is written in Java (using the [Jena RDF
toolkit](http://jena.apache.org/)), so you will need Java installed on
your system.

The default demonstration setup for Elda has examples that use the
`data.gov.uk` data accessible from the SPARQL endpoint
`http://education.data.gov.uk/sparql/education/query`. To run those
examples, you will need to have open web access.

Downloading and starting Elda
-----------------------------

There are two ways to get Elda: either by downloading the pre-compiled
runnable demo `.jar` file, or by checking out the source code from its
GitHub project and then compiling the Java source.

The runnable `.jar` can be downloaded from
<http://repository.epimorphics.com/com/epimorphics/lda/elda-standalone>
and following through to a recent version, or the version current for
this page:
[elda-standalone-1.3.0.jar](http://repository.epimorphics.com/com/epimorphics/lda/elda-standalone/1.3.0/elda-standalone-1.3.0.jar).

If you want to explore the Elda code and have [git](http://git-scm.com/)
installed, you can copy the Elda repository:

    git clone https://github.com/epimorphics/elda.git

To compile the code, you will also need [Apache
Maven](http://maven.apache.org/):

    mvn clean package

After compiling, the runnable `.jar` file will be in
`./elda-standalone/target/elda-standalone-$VERSION-exec-war.jar`.

If you can't use port 8080
--------------------------

Your computer may already be using port 8080 for some other service. In
that case, you can start Elda from the command line (this won't work
when double-clicking in a file manager) with:

    java -jar standalone.jar -httpPort NNNN

Built-in example datasets and configurations
--------------------------------------------

Elda comes pre-packaged with some example configurations and an example
dataset. The sample URLs in the table below should all work if you have
Elda running on your local computer, on port 8080:

description

config file

sample URLs

one-template games example, local SPARQL endpoint.

[hello-world.ttl](https://github.com/epimorphics/elda/tree/master/elda-standalone/src/main/webapp/specs/hello-world.ttl)

[standalone/hello/games](http://localhost:8080/standalone/hello/games)

[standalone/hello/games.xml](http://localhost:8080/standalone/hello/games.xml)

two-template games example, local SPARQL endpoint.

[hello-again-world.ttl](https://github.com/epimorphics/elda/tree/master/elda-standalone/src/main/webapp/specs/hello-again-world.ttl)

[standalone/again/games](http://localhost:8080/standalone/again/games)

[standalone/again/publishers](http://localhost:8080/standalone/again/publishers)

[standalone/again/publishers.json](http://localhost:8080/standalone/again/publishers.json)

one-template education example, external SPARQL endpoint.

[tiny-education.ttl](https://github.com/epimorphics/elda/tree/master/elda-standalone/src/main/webapp/specs/tiny-education.ttl)

[standalone/tiny/doc/school](http://localhost:8080/standalone/tiny/doc/school)

multi-template education example, external SPARQL endpoint.

[mini-education.ttl](https://github.com/epimorphics/elda/tree/master/elda-standalone/src/main/webapp/specs/mini-education.ttl)

[standalone/mini/doc/school](http://localhost:8080/standalone/mini/doc/school)

[standalone/mini/doc/school/phase/primary](http://localhost:8080/standalone/mini/doc/school/phase/primary)

[standalone/mini/doc/school/100855](http://localhost:8080/standalone/mini/doc/school/100855)

full education example, external SPARQL endpoint.

[full-education.ttl](https://github.com/epimorphics/elda/tree/master/elda-standalone/src/main/webapp/specs/full-education.ttl)

[standalone/full/doc/school](http://localhost:8080/standalone/full/doc/school)

[standalone/full/def](http://localhost:8080/standalone/full/def)

[standalone/full/def/school](http://localhost:8080/standalone/full/def/school)

[standalone/full/doc/school?max-schoolCapacity=100](http://localhost:8080/standalone/full/doc/school?max-schoolCapacity=100)

...

old education example [for comparison], external SPARQ endpoint.

[old-education.ttl](https://github.com/epimorphics/elda/tree/master/elda-standalone/src/main/webapp/specs/old-education.ttl)

[standalone/old/education/schools/primary](http://localhost:8080/standalone/old/education/schools/primary)

[old/education/schools?label=Hope+Primary+School](http://localhost:8080/standalone/old/education/schools?label=Hope+Primary+School)

Games example -- web page
-------------------------

Let's look a little at the two-template games example,
`standalone/again`.

(Refer to [the cribsheet](cribsheet.html) for a summary of the action of
Elda's query parameters.)

The page <http://localhost:8080/standalone/again/games> shows us the
properties (as in the example data file) of several boardgames. This
HTML view allows you to explore the dataset. For example, *A Brief
History of the World* has publication date 2009. Clicking the
dropdown icon on that line restricts the results to only those
with that publication date -- the *Brief History* and
*Last Train to Wensleydale(. And we see (from
the URL bar) that the URL of this page has had a
*publicationDate=2009* query parameter added.

The dropdown menu also allows you to select games
whose publication date is the same as, or less recent than, *A Brief
History of the World*, or, separately, no older. In both cases, 
the URL changes to show the applied filter. Selected options can
be de-selected.

Similarly you can play with the value of `players`, *eg* restricting
the number to 4 will show only games that have a 4-player option
(not that it does not reject games that allow other numbers of
players). And you can apply more than one filter, perhaps asking
for seven-player games from before 2005.

You can also chose to sort according to the selected property or 
properties, *eg* by publication date or (rough) play time in 
minutes, or both. However, `players`, being
multi-valued, isn't really a good sorting candidate.

Games example -- configuration
------------------------------

We'll talk briefly through the LDA specification used for the games
example.

    #
    # A hello-world config.
    #

    # Assorted prefix declarations. Some of them are for the structuring
    # of the API config, others are for the vocabulary definitions.
    #

    @prefix api:    <http://purl.org/linked-data/api/vocab#> .
    @prefix dct:    <http://purl.org/dc/terms/> .

    @prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
    @prefix rdfs:   <http://www.w3.org/2000/01/rdf-schema#> .
    @prefix xsd:    <http://www.w3.org/2001/XMLSchema#> .

    # The elda namespace/prefix is for Elda extensions to the LDA specification.
    @prefix elda:   <http://www.epimorphics.com/vocabularies/lda#> .

    # Prefix for example-local entities.
    @prefix hello:  <http://localhost:8080/elda/vocabulary/>.

The prefixes declared here make the configuration more concise and are
used in the result model from the LDA query.

    # ---------------------------------------------------------------------
    # API config

    hello:hello-world-again a api:API
    ; rdfs:label "Hello World example #2 -- games"@en

`hello:hello-world-again` is declared as an API. The label is used as a
title in /api-config.

    # Name the SPARQL endpoint which supplies the data we present
    ; api:sparqlEndpoint <local:data/example-data.ttl>

The SPARQL endpoint for this API is a local file. (This is an Elda
extension to the LDA for testing and demonstration purposes.)

    ; api:defaultViewer api:labelledDescribeViewer

The default view for this API is the built-in `labelledDescribeViewer`,
which fetches all the properties of the item using a SPARQL DESCRIBE and
also fetches the labels of all the resource values this finds.

    ; api:viewer [a api:Viewer; api:name "none"; api:properties ""]

This defines a new view called *none* which shows no properties at all.
This can be useful as a starting-point when building a view on the URL
using the query parameter `_properties=`.

    ; api:variable
      [ api:name "_resourceRoot"
      ; api:value "http://localhost:8080/standalone/lda-assets/"
      ]

The special variable `_resourceRoot` is used by Elda as the base URI for
assets needed for the HTML rendering of results.

    ; api:endpoint
      hello:publishers, hello:games

These are two endpoints for this API, `hello:games` and
`hello:publishers`.

    ; api:formatter
      [ a api:VelocityFormatter
      ; api:name "html"
      ; api:mimeType "text/html; charset=utf-8"
      ]
      .

(An alternative (older) formatter which generates its results
using XSLT stylesheets, is also available.)

    # Endpoint definitions

    hello:publishers a api:ListEndpoint
      ; rdfs:label "Publishers"
      ; api:uriTemplate "/publishers"
      ; api:selector [api:filter "type=Publisher"; api:sort "label"]
      ; api:defaultViewer api:labelledDescribeViewer
      .

This is the *publishers* endpoint. It will show only items of type
hello:Publisher, sort them by their label (which will be the publisher
name), and view them with the labelledDescribe viewer.

    hello:games a api:ListEndpoint
      ; rdfs:label "Games"
      ; api:uriTemplate "/games"
      ; api:selector [api:filter "type=BoardGame"; api:sort "label"]
      ; api:defaultViewer api:labelledDescribeViewer
      .

Similarly, this is the *games* endpoint, selecting only items of type
hello:BoardGame and sorting them by their label.

The rest of the configuration defines property and class shortnames and
other important attributes. The resouces (classes and properties both)
that are of interest are those of type `rdf:Property`,
`owl:ObjectProperty`, `owl:DatatypeProperty`, `rdfs:Class`, and
`owl:Class`.

    hello:BoardGame a rdfs:Class
    ; rdfs:label "Board Game"
    ; api:label "BoardGame"
    .

The class `hello:BoardGame` has shortname *BoardGame*, as defined by its
`api:label`; this is what allows it to appear in the endpoint filter
`type=BoardGame`.

    hello:players a rdf:Property
    ; api:label "players"
    ; rdfs:range xsd:int
    .

`hello:players` has shortname *players*, which is what allows you to use
*eg* `players` as the name of a query parameter. Defining its range to
be `xsd:int` means *eg* that the 2 in the query parameter `players=2`
is interpreted as an integer and not a string.

    hello:Publisher a rdfs:Class
      ; api:label "Publisher"
      .

Similarly to `BoardGame`.

    dct:publisher a rdf:Property
      ; api:label "publishes"
      .

Declares that the shortname of `dct:publisher` is *publishes*.

    rdfs:label a rdf:Property
      ; api:multiValued true
      ; api:label "label"
      .

Declares that `rdfs:label` has shortname *label* and that it is always
to be presented in the JSON rendering as having an *array* of label
values, even if there's only one.

    rdf:type a rdf:Property
      ; api:multiValued true
      ; rdfs:range rdfs:Class
      ; api:label "type"
      .

Declares that `rdf:type` has shortname *type*, should have its
JSON-rendered values appear as an array, and has a range of
`rdfs:Class`, which means that for a query parameter `type=SPOO`, SPOO
is interpreted as being the shortname of some resource rather than a
literal string.

    hello:designed-by a rdf:Property
      ; api:label "designedBy"
      .

Declares that `hello:designed-by` has shortname *designedBy*. (Note that
the HTML renderer shows property shortnamesin the display translated by
turning camelCase like *designedBy* into space-separated words like
*designed by*.)

    hello:designer-of a rdf:Property
      ; api:label "designerOf"
      .

The shortname of `hello:designer-of` is *designerOf*.

    hello:pubDate a rdf:Property
      ; api:label "publicationDate"
      ; rdfs:range xsd:integer
      .

And finally, the shortname of `hello:pubDate` is *publicationDate* and
its value is interpreted as an integer.

That concludes our example LDA specification. For more details on LDA
configurations, see [the Linked Data API
(LDA)](http://code.google.com/p/linked-data-api/wiki/Specification) and
[the Elda reference material](reference.html). Also see the other API
configurations mentioned above.

Using other LDA specifications
==============================

From the command line
---------------------

To use different LDA specifications, use the system property
`elda.spec`. Like the port number, it can be set as part of launching
the elda jar or when launching the jetty start jar:

    java -jar standalone.jar -Delda.spec=SPECS

If you use `elda.spec`, then Elda ignores the default specification (the
education LDA) wired into it.

By editing web.xml
------------------

To change the specification used without having to use a `-Delda.spec`
command line option every time, you will have to build your own
version of Elda; see the instructions elsewhere (TBD). When you
do so, edit `webapps/elda/WEB-INF/web.xml`. Find the servlet 
configuration

    <servlet>
      <servlet-name>loader-init</servlet-name>
      <servlet-class>com.epimorphics.lda.routing.Loader</servlet-class>
      <init-param>
        <param-name>com.epimorphics.api.initialSpecFile</param-name>
        <param-value>assorted specs here</param-value>
      </init-param>
      <load-on-startup>1</load-on-startup>
    </servlet>

and replace the *assorted specs here* with the name of the LDA
specification files you wish to load, separated by commas. Spaces and
newlines are ignored. (For more explanation of the structure of the
`web.xml` file, see [the reference documentation](reference.html)).

Older XSLT formatter
====================

To use the older XSLT renderer (typically if you have existing 
editing stylesheets or prefer the older rendering style), define
an xslt formatter:

    api:formatter
      [a api:XsltFormatter
      ; api:name 'html'
      ; api:stylesheet 'xslt/result-osm.xsl'
      ; api:mimeType 'text/html'
    ]

to your API root in your configuration file and arrange that the
webapps/elda directory contents are served as static files.

Alternative data sources
========================

Querying a local file
---------------------

If the remote SPARQL endpoint is slow, not yet fully configured, or
plain unimplemented, you might want to set up a local endpoint using a
tool like [Fuseki](http://openjena.org/wiki/Fuseki). But it's also
possible for Elda to query a local RDF file.

Edit your spec file, which will look something like the education spec
suplied with Elda:

    spec:api
      a api:API ;
      rdfs:label "Edubase API"@en;
      api:maxPageSize 50;
      api:defaultPageSize 10 ;
      api:sparqlEndpoint <http://education.data.gov.uk/sparql/education/query> ;
      api:endpoint
       spec:schools
       , spec:schoolsPrimary
       , spec:schoolsSecondary
       , spec:schoolsPrimaryDistrict
       , spec:schoolsSecondaryDistrict
     .

Replace the endpoint URI with **local:content-name**, where
**content-name** is the name of the RDF source you wish to query. When
Elda issues queries to a local: endpoint, it loads (and remembers) the
contents and queries those directly.

Usually the content-name is a file name. It is resolved against the
webapps context path (ie the directory from which it serves files). If
there is no such file, then the content-name is treated as a URL and its
contents fetched. If that fails, then the content is searched for along
the webapps classpath. In all of these cases, the fetched content is
loaded into memory as an RDF model and all queries to the endpoint are
answered by this model.

(The name-lookup functionality is supplied by the underlying Jena
`FileManager` class; for more details, see the Jena documentation
currently at openjena.org.)

Notes
=====

Feedback
--------

Elda aims to provide a complete implementation of the Linked Data API.
Problems with Elda should be reported to the [linked data API discussion
group](http://groups.google.com/group/linked-data-api-discuss) (note:
you will need a google account to use this group).

Current issues can be seen on the [Elda issues
list](https://github.com/epimorphics/elda/issues).


