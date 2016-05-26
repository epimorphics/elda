---
title: Elda Reference
layout: default-toc
---

Elda Common
===========

The stand-alone jar serves as a demonstration tool and small-scale Elda
service. However, the preferred way of delivering an externally-viewed
scalable Elda service is to use Elda Common (or write your own webapp
based on the Elda artifacts, of course).

Elda Common provides a way of dropping Elda into an existing webapp
container with a minimum of fuss. (While we write "Tomcat" below as the
webapp container, any suitably configurable one — *eg* Jetty — will do.
We assume that you have the necessary access permissions to install or
update Tomcat and run it on your preferred port.)

There are three components to Elda Common:

-   The Elda Common War, a Maven artifact you can download in the usual
    way as file `elda-common-VERSION.war`.
-   The Elda Assets War, a Maven artifact you can download in the usual
    way as `elda-assets-VERSION.war`.
-   [Minimal LDA
    configurations](https://raw.githubusercontent.com/epimorphics/elda/master/elda-assets/src/main/webapp/specs/ROOT_minimal.ttl)
    demonstrating Elda Common setup.

The Common and Assets files must have the same version.

To use Elda Common, you add both .war files to your Tomcat
configuration. Doing so may be as simple as dropping them into the
appropriate `webapps` directory. You need both .war files; Elda Common
contains the LDA-handling Java code and Elda Assets provides styleheets,
scripts, css, *etc* which Elda Common refers to or loads. 

By default, Elda Common assumes that Elda Common's context path is
`elda-common` and Elda Assets's context path is `elda-assets`. If you
want them to use different context paths, you will need to also change
the name and contents of the LDA config (see below). You can install
multiple copies of Elda Common with different context paths, all sharing
the same assets. (You can also have multiple different assets if
necessary.) You can install Elda as ROOT.war so that Tomcat is serving
the URI templates with no prefix, so long as the name of the assets .war
does not collide with any of those templates.

Elda Common loads its LDA configurations from
`/etc/elda/conf.d/{APP}/*.ttl`, where `{APP}` is the context path of
that instance of Elda Common. This means that each instance may have
multiple configs that it can load, and different instances can load
different files. (To see how this path is set, open up the .war
file and look inside at its `web.xml`.)

The LDA configurations contain references to the assets webapp, as seen
in the minimal configuration file supplied. For a given `API:api` in a
configuration file, the location of the assets is given in a variable
binding:

    ; api:variable [api:name "_resourceRoot"; api:value "http://hostAndPort/assetPath/"]

The *hostAndPort* will usually be **localhost:8080** or
**localhost:80**, and *assetPath* will by default be **elda-assets**
unless the asset .war is renamed to some other context.

Similarly, if the configuration is generating HTML using the Elda XSLT
renderer, the stylesheets are loaded from the assets according to the
binding on the XsltFormatter by:

    ; api:stylesheet "http://hostAndPort/stylesheetPath/xslt/result.xsl"

Again, if the stylesheet has moved, this binding must be changed
accordingly.

Elda provides some example minimal configurations, different only in
their intended environments:

<table class="table table-striped table-condensed">
  <tr>
    <td><a href="specs/minimal-named-or-ROOT-split-8080-config.ttl">minimal-named-or-ROOT-split-8080-config.ttl</a></td>
    <td>split assets and common on port 8080, for both ROOT and non-ROOT use.</td>
  </tr>
  <tr>
    <td><a href="specs/minimal-named-split-80-config.ttl">minimal-named-split-80-config.ttl</a></td>
    <td>split assets and common on port 80 for non-ROOT use.</td>
  </tr>
</table>

The configuration itself is a place-holder with three uri templates:

<table class="table table-striped table-condensed">
  <tr>
    <td><code>/anything</code></td>
    <td>to provide some example results</td>
  </tr>
  <tr>
    <td><code>/about?resource={someURI}</code></td>
    <td>provides information (ie properties and their values) about the
      resource <code>someURI</code></td>
  </tr>
  <tr>
    <td><code>/mentions?resource={someURI}</code></td>
    <td>finds items which have <code>someURI</code> as the value of some property.</td>
  </tr>

</table>

The minimal configuration assumes that a SPARQL endpoint is available on
`localhost:3030/store`. You can replace this endpoint with a different
one, or for development purposes you can run a
[Fuseki](http://jena.apache.org/documentation/serving_data/) SPARQL
server serving RDF data of your choice.

Reload on change
----------------

When Elda handles a request, if it has been "sufficiently long" since
the last request, it will check to see if it is up-to-date with its
configuration and, if not, reload it.

"Sufficently long" defaults to 5 seconds and can be adjusted by creating
the file `/etc/elda/conf.d/{APP}/delay.int`. `APP` is the context path
for this Elda webapp. The content of the file must be an integer number
of millseconds.

"Up-to-date" means that none of the configuration files or their
directories are time-stamped later than the time that the configurations
were loaded.

Elda does not check that the *contexts* of these files have changed, so
it is sufficient to simple `touch` one of the appropriate files.

Inside Elda Common
------------------

If you have downloaded the Elda repository, then you can rebuild the
Elda working jar (lda-VERSION.jar) and webapp using
[Maven](http://maven.apache.org/); see below.

Anatomy of Elda's web.xml {#anatomy}
-------------------------

Elda runs as a bunch of JAX-RS resources within a Jersey container. The
LDA configurations it loads are specified by the value of a *context
parameter*.

    <context-param>
       <param-name>com.epimorphics.api.initialSpecFile</param-name>
       <param-value>/etc/elda/conf.d/{APP}/*.ttl</param-value>
    </context-param>

For non-Common uses of Elda, you can have multiple comma-separated
configuration directives in a single \<param-value\>. Spaces, tabs, and
newlines within the value are discarded.

Each directive is a filename, with an optional leading prefix
specification consisting of a name followed by `::`.

If the prefix is supplied, then all of the URI templates in the
configuration are implicitly prefixed with it; this allows different
configurations to be loaded together even if they happen to share URI
templates.

The provided filename is a webapp-relative unless it starts with "/".
Any occurence of the string "{APP}" is replaced by the context path, and
the character "\*" matches any sequence of characters, allowing multiple
configuration files to be specified at one time and appropriately to
this webapp.

The string `{file}` in the prefix is replaced by the base of the
filename with any trailing `.ttl` removed. The string `{api}` in the
prefix is replaced by the local name(s) of the API resouce(s) in the
spec.

If the prefix contains a '\*' character, it is replaced by whatever
characters matched any wildcard '\*'s in the last segment of the
filename. (If there is more than one such '\*', the matching groups

#### Example

Given the directive `A*B::/etc/elda/*.ttl` and files `x.ttl`, `y.ttl`
and `z.text` in `/etc/elda`, the configuration file `x.ttl` will be
loaded and given prefix `AxB` and the file `y.ttl` will be loaded with
prefix `AyB`. `z.text` is ignored as it does not match the filename.

The Jersey servlet/filter {#servlet}
-------------------------

Elda runs Jersey either as a servlet or as a filter applied to all of
the paths for this context path. A path that does not match any LDA URI
template produces a NOT FOUND status that by default is handled by
Tomcat to serve a static file if one exists with that pathname. JAX RS
resources are located in the package `com.epimorphics.lda.restlets`.

     <filter>
        <filter-name>Jersey Web Application</filter-name>
        <filter-class>com.sun.jersey.spi.container.servlet.ServletContainer</filter-class>

        <init-param>
          <param-name>com.sun.jersey.config.property.packages</param-name>
          <param-value>com.epimorphics.lda.restlets</param-value>
        </init-param>

        <init-param>
          <param-name>com.sun.jersey.config.feature.FilterForwardOn404</param-name>
          <param-value>true</param-value>
        </init-param>

        <init-param>
             <param-name>com.sun.jersey.spi.container.ContainerRequestFilters</param-name>
             <param-value>com.sun.jersey.api.container.filter.PostReplaceFilter</param-value>
         </init-param>
    </filter>

    <filter-mapping>
        <filter-name>Jersey Web Application</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <servlet-mapping>
        <servlet-name>default</servlet-name>
        <url-pattern><b>/*<b></url-pattern>
    </servlet-mapping>

    <listener>
      <listener-class>com.epimorphics.lda.restlets.RouterRestlet$Init</listener-class>
    </listener>

The (optional) `Init` listener forces Elda to load its configurations
when the webapp starts. If this listener is omitted, Elda will load
configurations when the first request arrives.

Everything else is handled by the default servlet, which delivers all
the static files inside the webapp.

URL rewriting {#rewriting}
-------------

Elda Common does not do URL rewriting; it handles all of the requests
sent to its context, either as URI templates or as static files. For
applications that need to present different URIs to an external
interface from those used within the application, you can either use on
of the existing within-servlet URI rewriting engines or having a
front-end server, *eg* Apache or Nginx, which selectively rewrites or
discards requests with (un)suitable URIs.

Free-text searching {#text-search}
===================

Normally, specifying a query parameter `sn=v` to Elda (or using the
equivalent `api:filter` in the configuration file) requires that for an
item to be selected, it must have a value for the property whose
short-name is `sn` and whose value is `v`. This allows you, for example,
to find an item whose rdfs:label is "hello there" by using the query
parameter `label=hello%20there`, assuming `label` is the short-name of
`rdfs:label`.

However, if you want to find items whose (*eg*) `rdfs:label` text
*contains* **hello**, or which contains either **hello** OR **goodbye**,
*etc*, then you have to write explicit SPARQL `FILTER`s, probably using
regular expressions, and smuggle them into your configuration. This
works, and is very flexible, but is also rather inefficient, since *all*
the literal values of the desired property have to be examined, and
general regular expressions are quite complicated to process.

One solution to this is *indexing*. The literal text value of the RDF
properties is preprocessed, typically by breaking it up into words, and
an index constructed relating each word to all the literals it appears
in. Then a search string can likewise be broken up into words and the
search limited to literals that are related to those words. This can be
made much faster than a scan-and-regexp -- indexing can be done in
advance and updated incrementally as new RDF is added to the dataset.

Two related examples of text indexing and search are [Apache
Lucene](http://lucene.apache.org/core/) and [Apache
Solr](http://lucene.apache.org/solr/). Recent [Jena
Fuseki](http://jena.apache.org/documentation/serving_data/) SPARQL
servers offer an abtraction layer over these text search engines, and
Elda can exploit this. A special property (usually called `text:query`)
matches literals using the text engine rather than RDF literal lookup.

If the query parameter `_search` is defined, Elda uses its value as the
search string for the `text:query` property. The configuration file can
specify further details of how this transformation is performed. If the
SPARQL endpoint understands the `text:query` property (it need not be a
Fuseki, so long as it understands the same protocol), then the text
search engine will handle the query.

Using text search is a three-step process; deciding what properties are
to be indexed under what names; building (for the server) the index; and
using `_search` to make a query against that index.

We sketch these three steps below, but for details of the indexing and
query operations, see the references search/Lucene/Solr references
above. [Another document](search-example.html) provides a worked example
of creating a simple text-search-enabled Elda. See [Text searches with
SPARQL](http://jena.apache.org/documentation/query/text-query.html) for
more about the text-query module.

Dataset indexing
----------------

A Fuseki dataset can be configured to *index* specified properties
within an RDF model. The properties are associated with named *fields*
in the index. One of these fields can be designated as the *default*
field, the field that is searched if no field is explicitly given in a
search query; another can be designated as the *uri* field that holds
the URI of the resource associated with the search strings. For example,
the configuration in the Jena text search documentation makes the
default field name *text*, has it track the value of the property
*rdfs:label*, and binds the URI of the item to the field *uri*:

    ...
    <#entMap> a text:EntityMap ;
        text:entityField      "uri" ;
        text:defaultField     "text" ;
        text:map (
             [ text:field "text" ; text:predicate rdfs:label ]
             ) .
    ...

The indexing process breaks the text (of the objects of the properties)
into words and discards unhelpful terms such as "a" and "the" because
they do not discriminate well. It may -- depending on details of the
Lucene/Solr configuration -- do stemming and synonym indexing as well.
These words are the terms that can be used in the search.

Text query
----------

The value of the `_search` query parameter is passed through to Fuseki
and used as a *query* against the index. Just as the indexing operates
on text that has been broken up into words, the query isn't just a
string to search for: it is expressed in it own little language, as
defined by Lucene and very similarly by Solr (the Solr query language is
mostly a modest extension to the Lucene language). The simplest query is
a single word, eg *Steam* or *Mahal*, which will succeed for resources
that have that word in one of their property values.

> (The indexing/query process may work harder than this for match, eg by
> looking for stems or synonyns of words, but these details are endpoint
> and implementation specific.)

The query can contain several words; if so, it is looking for *any* of
those words. To force it to look for both, use the infix operator *AND*,
which you can also spell *&&*. The query *Age Steam* will find resources
that mention either of *Age* or *Steam*; the query *Age && Steam* will
find only resources that mention them both. Note that if you use the &&
syntax in a URI it will have to be carefully escaped, so *AND* is
probably the better choice.

Prefxing a word with *+* demands that it appear *somewhere* in the
indexed text (ie, the RDF model or any extra indexing text that the
endpoint applied). Prefixing the word with *-* demands that it does
*not* appear.

The query runs against the default field unless otherwise specified
using a *fieldName:* prefix. This prefix applies to the next (as short
as possible) piece of the query, so *alpha: Age Steam* looks for *Age*
in the *alpha* field and *Steam* in the default field. To make it apply
to both terms, you can either repeat the field, *alpha: Age alpha:
Steam*, or use parentheses for grouping, *alpha: (Age Stream)*. (You can
also use parentheses in the usual way for grouping mixtures of `AND` and
`OR`, etc.)

The query notation supports several other operators, and escapes so that
operators like *+* can be treated as ordinatry characters; for
information about these see eg [the Lucene classic package
summary](http://lucene.apache.org/core/4_4_0/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#package_description).

Configuring search
------------------

The default behaviour of `_search` is that its value is used as a query
against the Fuseki index of the SPARQL endpoint. If the value does not
specify otherwise, the search is performed against the default field.
The property that this is derived from is defined when the text is
indexed.

`_search`'s behaviour can be configured by properties attached to the
SPARQL endpoint, the api:API itself (over-riding those on the SPARQL
endpoint), or on individual API endpoints (over-riding the API and
SPARQL endpoint). The most general way to do this is to use:

    :APIorEndpoint elda:textSearchOperand (... "?_search" ...)

The elements of the list can be an integer `N`, a resource `P` which
should be the name of a property, a literal string `L`, or the special
literal string `?_search`.

-   `N`. This restricts the number of results matched by the text search
    to `N`. If omitted, there is no restriction.
-   `P.` The search is performed with the default field being the field
    that is associated with `P` in the index.
-   `L.` The value of `_search` is ignored and `L`'s lexical form is
    used as the search query.
-   `"?_search".` This is replaced by the value of `_search` and will be
    used as the search query.

Rather than using this most general mechanism, specifying

    :APIorEndpoint elda:textContentProperty some:Property

will make the default field be that associated with `some:Property` in
the same way as `P` in the general form above.

It is also possible (but not expected) to be able to change the property
used for text query by setting

    elda:textQueryProperty some:Property

By default, the `?item text:query literal` triples in the generated
SPARQL query are positioned first. If you have reason to believe that
your text search queries will be processed more efficiently by
positioning them later (just before the type triples), then setting

        elda:textPlaceEarly false

in the configuration will do so. Setting it `true` is equivalent to the
default of setting it early. (This assumes that the SPARQL processor
won't rearrange the query arbitrarily.)

search with shortnames
----------------------

Another way to use text search is to give `text:query` a shortname:

    text:query a rdf:Property
        ; api:label "search"
        .

Because `text:query` can accept a simple string object, this allows a
filter `search=something` to be used in the URL query parameters or in
an `api:filter` in the configuration file. This is especially useful
when the shortname appears at the end of a property chain such as
`author.search=mckillip`.

Caveats
-------

This feature is experimental and details may change.

The details of the query language -- and hence the meaning of the value
of the \_search query parameter -- may change between versions of Lucene
and Solr. These versions are dictated by the builder of the SPARQL
endpoint.

Query size control
==================

Some generated queries -- those that have view defined by property
chains and are applied to many selected items -- are rather large
(exceeding a megabyte). These queries are repetitions of the view
property accesses specialised by the selected item.

SPARQL 1.1 features
-------------------

Elda requires SPARQL 1.1 and exploits its VALUES feature. Elda
used to use nested selects if available to reduce query sizes
but this has been replaced by VALUES; the old 
**elda:supportsNestedSelect** flags, used to enable
nested selects, no longer affects the query and generates a
warning in the log if it is used.

DESCRIBE thresholds
-------------------

The use of **elda:describeThreshold** to specify whether a
nested-select should be used for big DESCRIBE queries is no
longer necessary and will generate a log message.

Additional Elda features
========================

Error pages
-----------

When Elda detects (or has detected for it) an error, it responds
with an appropriate status code (eg *BAD_REQUEST*) and an
*error page*. Earlier mechanisms for rendering an error page
have been revised in Elda 1.3.4.

The error page is a rendered by a velocity macro named one of:

* bad_request.vm 
* exception.vm  
* general_exception.vm  
* query_parse_exception.vm  
* stack_overflow.vm  
* unknown_shortname.vm  
* velocity_rendering.vm

according to the category of the captured exception.
It is searched for along the expanded Velocity path:

* the users configured _velocityPath
* the directory `/etc/elda/conf.d/APP/_error_pages/velocity/`
* the directory `_error_pages/velocity/` in the webapp
* the final fallback directory `velocity/` in the webapp

Typically (and in the case of Elda Common, specifically) it
will find the error page in the webapp's `_error_pages/velocity/`
directory, which provides a default rendering of the error.
However the developer may specify an alternative rendering
by supplying a file with the appropriate name in the 
`/etc/elda` error pages directory or by defining `_velocityPath`
and putting replacement error pages there.

The default error pages can be confgured to be `verbose` or
`taciturn` by setting the API variable `_errorMode` to
`"verbose"` or `"taciturn"`. The verbose rendering will 
supply additional information if available (*eg* the 
details of what made a request bad); the taciturn rendering
says as little as possible.

The variable `_message` is bound to the text of the diagnostic
message carried by the captured exception.

Configuration variables {#variables}
-----------------------

Elda reserves LDA variable names that begin "\_" for configuration
purposes.

While the LDA spec says ([see API Binding
Variables](http://code.google.com/p/linked-data-api/wiki/API_Binding_Variables))
that:

> endpoint-level variables can also depend on API-level variables, but
> not vice versa.

Elda specifically allows API-level variables to depend on endpoint
variables. This means that the value of an API variable cannot be
determined in advance (if it depends on any variables that may be bound
in endpoints, *eg* by appearing in a URI template). This turns out to
allow convenient idioms where API-level variable declarations assemble a
result from components that can appear elsewhere at the API level or
from the current endpoint.

Configuring Elda resource paths
-------------------------------

There are three resource paths that you may need to configure for Elda:
the *stylesheet path*, the *asset path*, and the *template path*.

### stylesheet path

The *stylesheet path* is used by the XSLT-driven HTML renderer to locate
the XSLT stylesheet to use. It appears as the object of an
`api:stylesheet` property on the HTML renderer, *eg*

    ... api:stylesheet "lda-assets/xslt/result-osm-trimmed.xsl"

If there are any variable references in the stylesheet value, they are
expanded. If the resulting stylesheet value starts with a scheme (*eg*
`http:` or `file:`) then it is treated as the URL of the stylesheet to
fetch. Otherwise it is treated as a reference to a file in the
application's `webapp` directory.

### asset path and `api:base`

The *asset path* is used by the XSLT-driven HTML renderer and
the velocity template renderer. It is the value of the LDA
variable `_resourceRoot`:

    ... api:variable [api:name "_resourceRoot"; api:value "lda-assets/"]

If that value is not defined, Elda uses the value of the `api:base`
property of the API spec:

    ... api:base "/"

`api:base` should, if possible, be used to specify where the Elda webapp
is served from; it is resolved against the URL for the current page so
that a server at location A can generate URIs as though it were at
location B.

If `api:base` is not defined, Elda uses `"/lda-assets"`.

When those renderers must generate a link in the HTML to some asset,
such as an image or some CSS, the path to that asset (*eg*,
`images/Star.png`) is prefixed with the asset path (*eg*, `lda-assets/`)
to construct the link URL (*eg*, `lda-assets/images/Star.png`). This URL
will then be resolved in the usual way, relative to the URL of the page
being rendered.

### template path

The *template path* is used by the velocity template renderer to locate
the templates it may expand. It is the value of the LDA variable
`_velocityPath`:

    ... api:variable [api:name "_velocityPath"; api:value "lda-assets/vm"]

Note that if the value is not an explict URI it is resolved *locally*
(the same way as `api>stylesheet`) against the webapp directory, as
opposed to `_resourceRoot`, who's value is resolved *remotely* by the
client browser.

The template that is used for a given velocity-template rendering is
given by the `elda:velocityTemplate` property of that renderer:

    ... elda:velocityTemplate "some-template-name.vm"

If no `velocityTemplate` property is defined, Elda uses
`"page-shell.vm"`.

### Other variables used by the stylesheets

-   activeImageBase: the location within the resource root where the
    active (enabled) images used by the stylesheet are fetched from.
-   inactiveImageBase: the location within the resource root where the
    inactive (disabled) images used by the stylesheet are fetched from.

### Variables configuring rendering used by the Elda code

-   \_suppress\_ipto: unless this variable is defined and has the value
    "yes", the meta-data (and hence the complete rendered model) for an
    item endpoint will include an `isPrimaryTopicOf` property of the
    selected item with value the currently displayed URI.

-   \_suppress\_media\_type: If there is no .formatter suffix in the
    request URL, and no \_format= query parameter, then the prescribed
    LDA behaviour is to see if the request headers specify an
    appropriate media type. If the requesting entity is a browser it
    almost always will (with \*/\*), which is inconvenient when testing
    for the default default behaviour of sending JSON. Setting this
    variable to "yes" will cause Elda to ignore the supplied media type.

-   \_exceptionIfEmpty: by default, if a query for an item template
    returns no item (because the requested item has none of the required
    properties) Elda will generate a 404 response rather than displaying
    an empty item endpoint page. If the variable \_exceptionIfEmpty does
    not have the value "`yes`", then the empty page is displayed.

Wildcard '\*' in view property chains {#wildcard-properties}
-------------------------------------

A view definition (either some `api:Viewer` in the configuration, or an
implicit one constructed from `_view` and `_properties` in the request
URL) is usually characterised by some collection of *property chains*.
Each property chain `S1.S2...` corresponds to a SPARQL query fragment
`?item P1 ?V1. ?V1 P2 ?V2...` where `Si` is the shortname of property
`Pi`. A property chain may be specified as the list object of an
`api:property` declaration or as a (comma-separated list of)
dot-separated list of property shortnames in the string value of an
`api:properties` declaration.

(A property shortname is the `api:label` of that property, or it's
`rdfs:label` if if has no `api:label` and its label has the required
syntax.)

Elda additionally allows an `api:properties` to contain the special
element `*` in place of a shortname, meaning "any property"; in the
generated query fragment, the `*` is replaced by a fresh variable. There
can be any number of `*` elements, anywhere within the chain, *eg*

        ... api:properties "type.*,label"
        ... api:properties "*.designedBy"

This also applies to the value of the reserved query parameter
`_properties`.

**Warning**. Because `*` can match any property, it can significantly
increase the size of the resulting model and the time taken for the
SPARQL endpoint to generate it. Use with caution.

URI rewriting
-------------

(*This feature is experimental and its details may change.*)

An Elda configuration may have rewrite rules associated with it. These
rules are applied to the viewed resources before the model is supplied
by the renderer. The intention is that during configuration development
for applications where the SPARQL data contains URIs that correspond to
application pages, the "official" URIs present in the data will be
rewritten to "local" URIs which, when they are used as the target of an
HTTP GET, will retrieve local data.

All the URIs in the view model are rewritten, including the datatypes of
typed literals. Lexical forms, language codes, and blank node IDs are
not rewritten.

The rewrite rules are specified by `elda:rewriteResultURIs` properties
of the LDA config and apply to all (and only) endpoints of that config.
There can be arbitrarily many such properties. The value(s) of the
properties are resources (typically blank nodes) with the properties
`elda:ifStarts` and `elda:replaceStartBy`, for example:

    ... ; elda:rewriteResultURIs
        [ elda:ifStarts "http://education.data.gov.uk/"
        ; elda:replaceStartBy "http://localhost:8080/elda/"
        ]

URIs starting with the value of the `ifStarts` property are rewritten by
replacing the `ifStarts` value with the `replaceStartBy` value, so given
the rewrite rule above,

    http://education.data.gov.uk/doc/school

will be rewritten to

    http://localhost:8080/elda/doc/school

If multiple `ifStarts` values match, the longest is preferred. It is an
error for multiple rules to share an `ifStarts` value.

elda:describeAllLabel {#describe-all-label}
-----------------------

If a new viewer is declared with the property `elda:describeAllLabel`,
it becomes a variant of the `describeAllViewer` where the label property
used is the object of that property rather than `rdfs:label`.

Multiple `describeAllLabel` properties may be specified. All such
available label properties and their values are available to
the view.

elda:allowedReserved {#allow-reserved}
----------------------

Normally (and as prescribed by the spec) Elda will generate a 400 status
for queries that try and use unknown reserved parameter names (those
beginning with \_), eg `?_example=17`.

The property `elda:allowReserved` may be attached to an API or to an
endpoint. Its values are the names of reserved parameters that should be
ignored rather than generating status 400.

Attachments to the API apply to all endpoints; attachments to one
endpoint affect only that endpoint. Elda automatically makes the
parameter name "\_" allowed, since it is often used in JASONP queries.

**Etag** generation {#etags}
-------------------

If an endpoint has the property `elda:enableETags` with value `true`, or
it does not have that property but its parent API spec does with value
`true`, then Elda will generate an etag on successful responses. The
value of the etag is derived from hashes of:

-   the request URI
-   the Accept header (if present)
-   the Accept-Encoding header (if present)
-   the response's media type
-   the content of the model

purging filter values
---------------------

(This is a new, experimental feature. Its details may change with
experience.)

To reduce the risk of carefully-crafted URLs offering a security risk
via an XSS attack, Elda allows the values of filters in the URL to be
*purged* by replacing suspicious characters by spaces.

If an endpoint has the property `elda:purgeFilterValues`, its boolean
value will determine if filters on that endpoint are purged. Otherwise,
if its parent API has an `elda:purgeFilterValues`, then the value of
that property determines if filters are purged. Otherwise, the default
value is `false` — values are not purged.

The default is likely to become true in later versions of Elda.

**Expires** headers {#expiry-headers-and-cache}
-------------------

(This is a new, experimental feature. Its details may change with
experience.)

By default, Elda does not generate any **Expires:** headers and assumes
that all data fetched from its SPARQL endpoint does not change while
Elda is running. This is sufficient if the data is fixed, or if Elda is
restarted when the data is "sufficiently old". An Apache or NGinx
wrapper can add the preferred **Expires:** headers.

If this is insufficient, *eg* data changes over periods of hours or
minutes, a *cache expiry time* can be associated with
each endpoint of the configuration. Endpoints with no explicit expiry
time use that of their parent API if it has one.

    ; elda:cacheExpiryTime Value

The `Value` can be an integer with units of seconds, or a string of
digits interpreted as the corresponding integer, or a string
`"integerLETTER"` meaning the specified number of the unit specified by
the LETTER:

s(seconds), m(inutes), h(ours), d(ays), w(eeks).

If an endpoint has a cache expiry time `T` (whether explicit or
implicit), then there are two consequences.

-   Responses from that endpoint will have an **Expires:** header. If
    this is a fresh response, then the expiry time is `E = now + T`. If
    this response is served from the Elda cache, then the expiry time is
    that of the cached response.
-   Newly generated responses are cached with the expiry time `E`. If a
    cached response is requested, and it has expired, that cache entry
    is dropped and a fresh response created, cached, and served.

This allows Elda to operate with its own (configurably-sized) cache but
still to serve controllably-fresh data from its SPARQL endpoint.

### Property-based expiry time

As well as setting explicit expiry times on endpoints or the API, they
can be set on declared properties in the configuration. A "declared
property" is one that has an `rdf:type` of `rdf:Property`,
`owl:ObjectProperty`, or `owl:DatatypeProperty`. Such declarations are
often present in a configuration as part of `api:label` shortname
declarations.

If a view mentions any properties with expiry times, then the smallest
such expiry time is taken as the upper limit on the expiry time for this
request. If the view contains any `*` wildcard properties, or uses a
`DESCRIBE`, then the smallest of any declared property's expiry time is
used.

This allows "volatile" properties to be annotated with their expiry
times and for those times to automatically propagate to any request that
uses them.

Item template fallback
----------------------

If an inbound URI does not match any of the uriTemplates of the
endpoints, Elda attempts to match that URI against any item templates of
the endpoints. If it finds a match, then the query is redirected to that
item endpoint.

This behaviour is currently not configurable.

Configuration rendering
-----------------------

Elda provides the api:base-relative URI path `/api-config`. Browsing
this URI delivers a rendering of the various APIs that the Elda instance
provides. Each API description shows the different endpoints, with their
variable bindings and named views, and the dictionary of shortnames
appropriate to this endpoint. By default the descriptions are hidden
(for compactness) and are revealed by clicking on the section titles.

The api:base-relative URI path `/meta/some/uri/template` provides the
same configuration description as `/api-config`, but the API and
endpoint for some/uri/template are already opened.

Specifying a dataset graph {#dataset-graph}
--------------------------

Normally Elda will query the default graph of the dataset being served
by its SPARQL endpoint. To query one of the named graphs of the datset,
an endpoint can be configured with a *graph template*.

    ...
    ; elda:graphTemplate "URI-with-{variables}"

The template is expanded with the values of any {}-enclosed variables
(including any set from the uri template match) and used to specify the
GRAPH for this query.

The API itself can be given a `graphTemplate`; this is used if the
endpoint does not have its own, allowing a default graph name to be
given for the entire configuration.

Any `graphTemplate` can be overridden by using the `_graph` query
parameter in the submitted URL; its value should be a suitably-encoded
URI which is used as the GRAPH name.

LogRequestFilter
----------------

By default (as configured in elda-standalone and elda-common,
see their `web.xml` files), Elda logs the beginning and ending 
of a request and gives it a (non-persistent) ID. Requests for 
static resources under `lda-assets` are not logged by default;
this is configurable using the `ignoreIfMatches` parameter
of the `LogRequestFilter`.

The ID is injected into the response headers and into
the API variable bindings under the name `_transactions`.

Warning: mixing graph specification and describe queries
--------------------------------------------------------

The specification of SPARQL's `DESCRIBE` is very loose and
implementations can legitimately differ in which parts of the dataset
are used to assemble the DESCRIBE result as well as what triples are
returned from a component graph. In particular, specifying a specific
named graph with `_graph` or a graph template does *not* mean that the
returned triples come only from that graph.

If it is important to your application that the results of a view are
restricted to a specified graph, use property chains rather than
describe viewers to define the view, with the `*` wildcard to imitate
the generality of DESCRIBE. While this will not have the full effect of
DESCRIBE's bnode closure it will provide predictable results.

Licence metadata
----------------

*Experimental*. Starting with Elda 1.3.18, page metadata may include
licence information, as the URI value(s) of the property `elda:licence`
of the page.

An API may declare licences using the (possibly multi-valued)
property `elda:licence`. Additional licences can be declared for 
individual endpoints of that API.

The objects of `elda:licence` may be named resources or literal strings. 

A named resource is attached using `elda:licence` to the page
metadata for this API or endpoint. All the properties of that
resource become part of the metadata. 

A string literal should consist of a dot-separated sequence of
shortnames (ie a property chain); a shortname may be preceeded by 
`~` to specify its inverse. The licence resource is found by following
the property chain starting from the selected items of the query
and is added to the page metadata with on level of property/values.

Note that this means an extra query to the SPARQL server is made
for every Elda query, and that no caching is applied to the
result. Hence an Elda query can take up to 5 SPARQL queries:

-   the query for selected items (always required)

-   the query for view resources from property paths

-   the query for DESCRIBEd resources

-   the query to find the names of all resouces fetched for
    the view

-   the query to find licences that apply to this page
 
Turtle, XML, and JSON renderings simply include the new licence
metadata; the client can harmlessly ignore it if required.

The default Velocity renderer has been modified so that the
footer page (footer.vm) checks for the presence of any licences
and if so displays them as a picture and a label.

-   the picture is the image (if any) which is the value of
    the `foaf:depiction` of the licence resource.

-   the label is the value of the `rdfs:label` property of
    the licence resource, if there is one, and otherwise
    the local name of the licence resource. 

-   the label is displayed as link with href the licence
    resource and body the link text.

A sequence of licence resources is available to velocity
macros as the value of the variable `_licences`. The individual
values are instances of `LicenceResource` which has methods
`getURI()` for the URI of the licence, `getLabel()` for the
label of the licence, and `getPicture()` for the image to
display for the licence.

Currently the HTML renderer does not display licence information.

Formatting extensions
=====================

Java renderer factories
-----------------------

If a formatter has the property
`http://www.epimorphics.com/vocabularies/lda#className`, then the
(String) object of that property must be the name of a Java class that
implements the `RendererFactory` interface. When rendering is required,
an instance of that class is invoked to deliver a Renderer, and that
Renderer is used to render the result set.

Predefined variables
--------------------

Formatters are passed an Elda `Bindings` object which gives access
to the values of API variables -- those defined in the LDA configuration
file, those arising from URL query parameters (a query parameter
`name=value` will (re)define the value of the variable `name`), and
those defined by Elda for use by renderers:

* _suffix: the renderer name for this renderer, *eg* `html`, `ttl`.

* _defaultSuffix: the name of the default renderer for this endpoint,
  or of the API spec as a whole if this endpoint does not define an
  default renderer. 

* _servletRequest: the Java object which is the HttpServletRequest
  for this request.

* _servletResponse: the Java object which is the HttpServletResponse
  for this request.

Both of the servelet values must be accessed using `getAny(name)`
rather than the more usual binding methods `get(name)`, 
`getAsString(name)`.  


The Atom renderer {#atom-feed}
-----------------

Elda contains an experimental atom-feed renderer. To use it, add a new
formatter to the configuration file:

    ...
    ; api:formatter
        [a elda:FeedFormatter
        ; api:name "atom"
        ; elda:className "com.epimorphics.lda.renderers.FeedRendererFactory"
        ; api:mimeType "application/atom+xml"
        ; elda:feedTitle "an example Elda feed"
        ]

The ID of the feed is the URI used to create it. The entries of the feed
correspond to the selected items of the query; the ID of an entry is the
URI of the corresponding item.

The title of the feed is set using the elda:feedTitle property of the
formatter in the LDA config, as in the example above. The title of an
entry is set from the value of the first of the properties in the list

    api:label
    skos:prefLabel
    rdfs:label

that is defined; this list can be changed by setting the value of the
formatter's `elda:feedLabelProperties` property.

The update time of a feed is the latest of the updated times of its
entries.

The update time of an entry is the value of the first property in the
list:

    dct:modified
    dct:date
    dct:dateAccepted
    dct:dateSubmitted
    dct:created

that is defined. This list can be changed by setting the value of the
formatter's `elda:feedFateProperties` property.

The content of an entry is similar to the XML rendering of the resource
for this entry. Currently **circularity checking is not available**. The
entry is given its own namespace, which defaults to the Elda namespace
but can be set in the feed configuration using the property
`elda:feedNamespace`.

The rights of an entry is the value of the first property in the list

    dct:rights

that is defined; otherwise there is no rights element. This list can be
changed by setting the value of the formatter's
`elda:feedRightsProperties` property.

The rights of a feed can be specified by giving a value to the
`elda:feedRights` property of the formatter.

The authors of a feed are specified by the list-valued property
`elda:feedAuthors`. The authors of an entry are specified by the values
of the first property in the list

    dct:creator
    dct:contributor

That has any. This list can be changed by setting the value of the
formatter's `elda:feedAuthorProperties`.

JSON LD renderer
----------------

As of version 1.3.18, Elda contains a JSON-LD renderer.
Add to your Elda formatter configuration:

    ...
    ; api:formatter
        [a api:JsonFormatter
        ; api:name "json-ld"
        ; elda:className "com.epimorphics.lda.renderers.JSONLDRendererFactory"
    #
    # use /json if client doesn't render json-ld as json, otherwise
    # use /json+ld. Firefox doesn't understand +ld so we'll force to /json
    # for the moment.
    # 
        ; api:mimeType "application/json"
        # ; api:mimeType "application/json+ld"
        ]

The JSON LD generated by the JSON LD renderer has a fixed
structure: an @context member containing the term bindings,
format and version metadata members, and a `results` member
for the selected items and their values.

The values of the properties of the selected items are
embedded inline as (nested) JSON values, except for
resources that are the objects of multiple statements
(or are selected items). Such resources are defined
in an additional top-level member `other`. 

If a property has multiple values or is declared to be
structured, then its JSON LD representation is an
array of JSON LD values.

Date & Times in JSON
--------------------

By default and according to the LDA spec, when Elda renders a
DateTime using JSON the result has the format 
`EEE, d MMM yyyy HH:mm:ss 'GMT'Z` if it is a `DateTime` or
`yyyy-MM-dd` if it is a `Date`.

Attaching the property `elda:jsonUsesISODate` to a JSON formatter
will leave this behaviour unchanged if its value is `false` but
will render the date using an ISO 8601 format if its value is 
`true`:

    `yyyy-MM-ddTHH:mm:ss`

The `T...` is ommited for `Date` values.

Statistics
==========

HTML display of statistics
--------------------------

The api:base-relative URI path `/control/show-stats` displays statistics
about the queries that this Elda instance has handled, including:

-   the total number of requests made
-   the number of requests that failed
-   the number of selection cache hits
-   the number of view cache hits

(Elda maintains two internal caches, one mapping the computed selection
query to the list of items it generates, the other mapping (list of
item, view) pairs to generated result sets. These are independant of any
caches provided by *eg* an Apache server wrapping Elda.)

-   the elapsed time dealing with all requests
-   the elapsed time taken for item-selection queries
-   the elapsed time taken for view-display queries
-   the elapsed time taken to render an Elda result
-   any remaining non-query non-rendering time

All of these results show the total time, the mean time over all
requests, and the maximum and minimum times over all requests.

-   the size of the rendered results
-   the size of the select queries
-   the size of the view queries

All of these results show the total size (in [kilo-]bytes), and the
mean, maximum, and minimum over all requests.

The display also breaks down rendering sizes and times by the rendering
format (ie JSON/XML/HTML ...).

JMX statistics
--------------

Elda can also serve limited JMX statistics (the same ones as are
accessible using `show-stats`) by enabling the appropriate listeners in
web.xml:

    <listener>
      <listener-class>com.epimorphics.lda.jmx.Statistics</listener-class>
      <listener-class>com.epimorphics.lda.jmx.CacheControl</listener-class>
    </listener>

This feature is experimental; it may be dropped in a future release.

Cache
-----

Elda caches the results of queries so that they may be re-served
quickly. When the cache gets "too full", it is reset. "Too full" by
default is measured by the number of triples in the cache; the default
limit is 20000 triples.

The cache policy can be changed by setting the property
`elda:cachePolicyName` to a string of the form `"name:integer"` where
`name` is the name of a policy, eg **default**, and the optional
`:integer` gives an associated limit.

The available cache policies are

-   `default` — same as limit-triples.
-   `limit-triples` — clear the cache when the number of triples
    retained is greater than the limit.
-   `limit-entries` — clear the cache when the number of entries is
    greater than the limit.
-   `perma-cache` — keep everything (not a good choice in production).

Cache policies can be attached to the API spec and over-ridden on
individual endpoints.

### external control of the Elda cache

If a request to Elda has a `pragma` or `cache-control:` header with
value `no-cache` (such as a CRTL/F5 refresh might cause on Firefox) then
that request is not served from the cache. The resulting response is
then cached.

The Elda-webapp-relative URL `control/show-cache` shows information
about the Elda cache state and provides two buttons: one to reset the
statistics count, and another to clear the Elda cache completely. (The
latter is exactly equivalent to POSTing to `control/clear-cache`.)

Shortnames in Elda
==================

Restrictions on shortname selection {#shortname-restrictions}
-----------------------------------

In an (E)lda configuration file, the configurer may define "short names"
for properties and resources. However, the current supplied example XSLT
stylesheets expect that the properties used to define the metadata about
a query and its results have certain specified shortnames.

Elda reserves and predefines those shortnames; the configuation writer
should not attempt to define them themselves.

The reserved names are the local names (except where otherwise
indicated) of the properties:

<table class="table table-striped table-condensed">
  <thead> <tr><th>prefix</th> <th>term</th> <th>term</th> <th>term</th></tr></thead>
  <tr> <td>rdf</td>  <td>type</td> <td>value</td> <td>&nbsp;</td> </tr>
  <tr> <td>rdfs</td>  <td>label</td> <td>comment</td> <td>&nbsp;</td> </tr>

  <tr> <td>xsd</td>  <td>integer</td> <td>decimal</td> <td>string</td> </tr>
  <tr> <td>xsd</td>  <td>boolean</td> <td>int</td> <td>short</td> </tr>
  <tr> <td>xsd</td>  <td>byte</td> <td>long</td> <td>double</td> </tr>
  <tr> <td>xsd</td>  <td>date</td> <td>time</td> <td>&nbsp;</td> </tr>

  <tr> <td>doap</td>  <td>implements</td> <td>releaseOf</td> <td>homepage</td> </tr>
  <tr> <td>doap</td>  <td>repository</td> <td>browse</td> <td>location</td> </tr>
  <tr> <td>doap</td>  <td>wiki</td> <td>revision</td> <td>&nbsp;</td> </tr>
  <tr> <td>doap</td>
    <td>bug-database <div>(as bug_database)</div></td>
    <td>programming-language <div>(as programming_language)</div></td>
    <td>&nbsp;</td>
  </tr>

  <tr> <td>opmv</td>  <td>software</td> <td>&nbsp;</td> <td>&nbsp;</td> </tr>

  <tr> <td>api</td>  <td>definition</td> <td>extendedMetadataVersion</td> <td>page</td> </tr>
  <tr> <td>api</td>  <td>items</td> <td>item</td> <td>processor</td> </tr>
  <tr> <td>api</td>  <td>property</td> <td>selectionResult</td> <td>termBinding</td> </tr>
  <tr> <td>api</td>  <td>variableBinding</td> <td>viewingResult</td> <td>wasResultOf</td> </tr>
  <tr> <td>dct</td>  <td>format</td> <td>hasFormat</td> <td>hasPart</td> </tr>
  <tr> <td>dct</td>  <td>hasVersion</td> <td>isFormatOf</td> <td>isPartOf</td> </tr>
  <tr> <td>dct</td>  <td>isVersionOf</td> <td>&nbsp;</td> <td>&nbsp;</td> </tr>
  <tr> <td>elda</td>  <td>listURL</td> <td>sparqlQuery</td> <td>&nbsp;</td> </tr>
  <tr> <td>foaf</td>  <td>isPrimaryTopicOf</td> <td>primaryTopic</td> <td>&nbsp;</td> </tr>
  <tr> <td>OpenSearch</td>  <td>itemsPerPage </td> <td>startIndex</td> <td>&nbsp;</td> </tr>
  <tr> <td>sparql</td>  <td>endpoint</td> <td>query</td> <td>url</td> </tr>
  <tr> <td>xhv</td>  <td>first</td> <td>next</td> <td>prev</td> </tr>

</table>

Elda shortname creation {#shortname-mode}
-----------------------

When Elda renders a result-set graph using JSON or XML (and consequently
HTML), URIs used as predicates are given short names if they have not
been given in the configuration. Where possible these names should
satisfy the shortname grammar; this allows them to be used as XML
element names. Elda splits the URI into its *namespace* and *local name*
parts and uses the localname as the basis for the generated shortname.

*NOTE*. Elda uses the Jena XML-derived rule to split URIs, defining the
local name as being the longest possible NCName that is a suffix of the
URI. That name is not necessarily the same as that after a '\#' or final
'/' of a URI. Because the local name is an NCName it is automatically a
legal XML element name.

Elda has three *shortname modes* for generating shortnames. The mode for
a given format may be specified by setting the property
`elda:shortnameMode` on a declared `formatter` of the configuration to
one of the values `elda:roundTrip`, `elda:preferLocalname`, or
`elda:preferPrefixes`. The default for all renderers is
`preferLocalname`.

Elda follows these rules for converting a URI (not already given a
shortname) with namespace N and local name L into a shortname:

1.  if the mode is `preferLocalname`, and this is the only URI with this
    local name, and L is not already a shortname, its shortname is L.
2.  if the namespace has an available prefix P and P\_L is not already a
    shortname, then its shortname is P\_L.
3.  if mode is not `roundTrip`, the shortname is L\_N, where N is a
    (weak) hash of the namespace.
4.  otherwise the shortname is a reversible encoding of the full URI
    into a shortname (that is not short).

The bindings of short names to full URIs — the *term bindings* — are
made available as metadata in the rendered result. This metadata is by
default only generated for HTML renderings; to have it present in JSON,
XML, or RDF renderers, specify the query parameter `_metadata=bindings`
or `_metadata=all` on the LDA request.

Item counts for result pages {#enabling item counting}
============================

Elda displays the items in list endpoints paged, with links to previous
and next pages embedded in the rendering, and the size of a page
controllable (within limits) by the reserved query parameter `_pageSize`
and the current page controlled by the reserved query parameter `_page`.
However by default the total number of items that could be selected is
not available.

Item counting can be enabled (see below), in which case Elda will add
additional meta-data to the result graph: the property `os:totalResults`
(where `os` is the OpenSearch prefix) will have as its value the total
number of results that the underlying query returns. The XSLT HTML and
the example Velocity renderers will incorporate this information into
their displays.

Item counting can be enabled by setting the property
`elda:enableCounting` on the API configuration root to `true` or
`"yes"`. This enables counting on any endpoint that does not override it
by setting the endpoint's `elda:enableCounting` to `false` or `"no"`.

If an endpoint has `elda:enableCounting` set to `"optional"`, either
explicitly or by inheriting from the root, then counting can be enabled
by setting the reserved query parameter `_count` to `yes` and explicitly
disabled by setting it to `no`. (Trying to use `_count` when
`elda:enableCounting` is not `"optional"` will result in a
`400 Bad Request` response.)

The count value is cached, so as the user moves forward and back along
next/prev links in the Elda response, the same count is re-used.

Using Elda directly {#using-elda-from-java}
===================

You don't need to go through a servlet (or restlet) framework to exploit
Elda. You can call the components yourself and supply whatever glue you
like. Note however that details of the code structure may change between
releases.

Creating an APISpec
-------------------

The constructor

    APISpec(FileManager fm, Resource config, ModelLoader forVocab)

delivers a new APISpec object configured from the given Resource. You
may have chosen a Resource with a known URI in a config model, or found
one with `rdf:type` `api:API`, depending on your usecase. The
`ModelLoader` is only used if the config has `api:vocabulary` elements,
in which case it loads the models for its API vocabulary. The
`FileManager` is used for accessing data sources.

Given an APISpec, the method `getEndpoints()` delivers a list of
`APIEndpoint` objects corresponding (in no defined order) with the
endpoint descriptions in the config model.

Running an endpoint
-------------------

You can then invoke

    APIEndpointUtil.call(APIEndpoint.Request r, Match match, String contextPath, MultiMap<String, String> queryParams)

where

-   *r* is a Request object describing this LDA request. A Request
    itself contains:
    -   a Controls object for timing information
    -   the request URI
    -   bindings of LDA variables to values
    -   the shortname creation mode
    -   the name of the rendering format
-   *Match match* wraps the endpoint object and the variable bindings
    that made when matching the endpoint's `api:uriTemplate` against the
    request URI. The usual way to get a Match object is to call
    `getMatch` on a suitable `Router` value.
-   *URI requestURI* is the request URI for this request.
-   *contextPath* is the context path to assume (it is written into the
    variable bindings)
-   *MultiMap queryParams* is a map from query parameter names to their
    (string) values.

The call returns a three-element object which contains the `ResultSet`
of the query execution (the result model and selected items), a map (the
*term bindings* from shortnames to their URIs as appropriate for this
resultset, and the (updated) variable Bindings.

Rendering results
-----------------

Once you have chosen a renderer `R` to use for the result set, the
invocation

    R.render( t, rc, termBindings, results )

where `t` is a `Times` object, delivers a String which is the rendering
of `results` according to the RenderContext `rc`, which you can
construct from the `VarValues` embedded in the call context, the context
path, and an AsURL object to convert URI fragments into full URIs. The
termBindings should be the map returned from `APIEndpointUtil.call`.

The method call `R.getMediaType()` returns the media type for the
renderer's result.

Building Elda
=============

Prerequisites: [Java](http://java.com/) (underlying platform),
[git](http://git-scm.com/) (to fetch the sources),
[Maven](http://maven.apache.org/Maven) (build management). Maven will
download remaining necessary jars for Jena, Jersey, etc.

Download the Elda sources:

    git clone https://github.com/epimorphics/elda.git

places the Elda sources in ./elda (which is created if necessary).

Running

    mvn clean install

will now build the Elda jars and put them into your local Maven
repository, along with all the jars that they depend on. You can then
either use Maven to build your own application with those jars as
dependencies, or extract them and embed them in your own libraries.

Look in the (automatically created) file
`/lda/src/main/java/com/epimorphics/lda/Version.java` to see which
version of Elda is being built. If you want to use a non-SNAPSHOT
version, use

    git checkout tags/REVISION

before running maven, where REVISION is your choice of the revision tags
you get from running:

    git tags

and selecting a tag that looks like `elda-1.X.Y`; that is the shape of
tag generated by the Elda release process.

As of Elda 1.2.23, the names of Elda modules (and the names of the
corresponding directories in the Elda sources) have changed.

Accepted content types
======================

Elda accepts the following content types by default.

-   text/javascript, application/javascript: for JSONP on some browsers.
-   text/plain: Plain text JSON.
-   application/rdf+xml, text/turtle: RDF/XML or Turtle representations
    of models.
-   application/json: model rendered as JSON.
-   text/xml, application/xml: model rendered as XML with whichever
    content type was asked for. (The renderings are identical.)
-   text/html: model rendered by stylesheet applied to XML rendering.

* * * * *

© Copyright 2011–2013 Epimorphics Limited. For licencing conditions see
<http://http://epimorphics.github.io/elda/LICENCE.html>.

