---
title: Elda cribsheet
layout: default-toc
---

Query Parameters
================

<table class="table table-striped table-condensed">
  <thead>
    <tr>
      <th>query parameter</th>
      <th>details</th>
      <th>operation</th>
    </tr>
  </thead>

  <tr>
    <td>P=V
    </td>
    <td>
      P is a property chain, <em>i.e.</em> a sequence
      of dot-separated short names of properties.
    </td>
    <td>
      Select only items which have a value V
      for the final item in the chain. (V's value
      string is converted according to the range
      of that property.)
    </td>
  </tr>

  <tr>
    <td>min-P=V
    </td>
    <td>
      P is a property chain.
    </td>
    <td>
      Select only items whose P-value is at least V.
    </td>
  </tr>

  <tr>
    <td>max-P=V
    </td>
    <td>
      P is a property chain.
    </td>
    <td>
      Select only items whose P-value is at most V.
    </td>
  </tr>

  <tr>
    <td>minEx-P=V
    </td>
    <td>
      P is a property chain.
    </td>
    <td>
      Select only values whose P-value is more than V.
      (minEx means "minimum, exclusive of V")
    </td>
  </tr>

  <tr>
    <td>
      maxEx-P=V
    </td>
    <td>
      P is a property chain.
    </td>
    <td>
      Select only values whose P-value is less than V.
    </td>
  </tr>

  <tr>
    <td>
      exists-P=true
    </td>
    <td>
      P is a property chain.
    </td>
    <td>
      Select only items which have some P-value.
    </td>
  </tr>

  <tr>
    <td>
      exists-P=false
    </td>
    <td>
      P is a property chain.
    </td>
    <td>
      Select only items which <em>do not</em> have
      a P-value.
    </td>
  </tr>

  <tr>
    <td>
      lang-P=LANG
    </td>
    <td>
    </td>
    <td>
    </td>
  </tr>

  <tr>
    <th>reserved query parameter</th>
    <th>details</th>
    <th>effect</th>
  </tr>

  <tr>
    <td>
      _pageSize=N
    </td>
    <td>
      N is an integer greater than 0.
    </td>
    <td>
      Show N items on this page.
    </td>
  </tr>

  <tr>
    <td>
      _page=N
    </td>
    <td>
      N is an integer greater than 0.
    </td>
    <td>
      Show page N, where a page has X items on
      it as defined by _pageSize=X or by the
      API configuration or as the default of 10.
    </td>
  </tr>

  <tr>
    <td>
      _format=NAME
    </td>
    <td>
      NAME must be the name of one of the formats understood
      by this Elda (<em>e.g.</em>, html, json).
    </td>
    <td>
      Render this query in the given format.
    </td>
  </tr>

  <tr>
    <td>
      _lang=LANG
    </td>
    <td>
      LANG is a language code, <em>eg</em> <em>en</em>, <em>eh-uk</em>,
      or the special language code <em>none</em>.
    </td>
    <td>
      Sets the default language(s), overriding anything set by the
      <code>api:lang</code> setting in the configuration.
      Query parameter values that are strings are given the
      default language code. The view will discard property values
      that are strings not in the the given languages, unless the
      only values are strings with <em>no</em> language, in which
      case those are displayed.
    </td>
  </tr>

  <tr>
    <td>
      _view=NAME
    </td>
    <td>
      NAME is the name of a view as defined in the configuration,
      or one of the predefined names <strong>description</strong>, <strong>all</strong>,
      or <strong>basic</strong>.
    </td>
    <td>
      Only the item properties specified in the view will be
      displayed.
    </td>
  </tr>

  <tr>
    <td>
      _properties=A,B,...
    </td>
    <td>
      A, B, ... are property chains P.Q.R, where P... are the
      short names of properties or the special wildcard property
      <code>*</code> meaning any property.
    </td>
    <td>
      The property chains are added to those of the view
      defined by <strong>_view</strong> or the configuration. (This
      applies even if that view is an <strong>all</strong> or
      <strong>description</strong> view which uses a SPARQL
      DESCRIBE to get property values rather than individual names.)
    </td>
  </tr>

  <tr>
    <td>
      _sort=A,B,...
    </td>
    <td>
      A, B, are comma-separated sort specifications: an optional
      <strong>-</strong> meaning "sort in reverse order" preceeding a
      property chain.
    </td>
    <td>
      The items are sorted according to the values of the specified
      properties.
    </td>
  </tr>

  <tr>
    <td>
      _orderBy=ORDER
    </td>
    <td>
      ORDER is a string suitable for use as a SPARQL ORDER BY
      clause. (This is useful only with <strong>_where</strong> as a
      query parameter or <code>api:where</code> in the configuration.)
    </td>
    <td>
      The items are ordered according to the ORDER BY clause.
    </td>
  </tr>

  <tr>
    <td>
      callback=F
    </td>
    <td>
      F must be a legal Javascript name.
    </td>
    <td>
      The result is a Javascript call <code>F(J)</code> where <code>J</code>
      is the JSON rendering of the result model.
    </td>
  </tr>

  <tr>
    <td>
      _metadata=NAME
    </td>
    <td>
      NAME should be one of the known metadata component names.
    </td>
    <td>
      That named metadata is included in the result model:

      <ul>
        <li>
          <strong>versions</strong> the available different views of this result.
        </li>
        <li>
          <strong>bindings</strong> the term (shortname) and variable bindings.
        </li>
        <li>
          <strong>execution</strong> details of the LDA processor, <em>i.e.</em> Elda,
          specifying where its code and documentation can be found.
        </li>
        <li>
          <strong>formats</strong> the available different formats of this result.
        </li>
        <li>
          <strong>all</strong> all of the above.
        </li>
      </ul>

      The XSLT-driven HTML renderer automatically incorporates all of these.
    </td>
  </tr>

  <tr>
    <td>
      _template=TEMPLATE
    </td>
    <td>
      TEMPLATE must be a legal SPARQL <strong>CONSTRUCT</strong> body also legal as a
      <strong>WHERE</strong> body.
    </td>
    <td>
    </td>
  </tr>

  <tr>
    <td>
      _where=CLAUSE
    </td>
    <td>
      CLAUSE must be a legal SPARQL <strong>where</strong> clause.
    </td>
    <td>
      CLAUSE is used in the SPARQL query rather than any computed or
      configured <strong>where</strong> clause.
    </td>
  </tr>

  <tr>
    <td>
      _search=QUERY
    </td>
    <td>
      QUERY must be a legal text-search query.
    </td>
    <td>
      All the selected items must satisfy the query.
    </td>
  </tr>

  <tr>
    <td>_graph=URI</td>
    <td>
        URI should be the (suitably-encoded) URI of a graph
        in the datatset being queried.
    </td>
    <td>
        The generated SPARQL query specifies the graph
        with that name.
    </td>
  </tr>

</table>

Configuration
=============

<table class="table table-striped table-condensed">
  <thead>
    <tr>
      <th>resource</th>
      <th>property</th>
      <th>value</th>
      <th>meaning</th>
    </tr>
  </thead>

  <tr>
    <td>an API</td>
    <td>api:sparqlEndpoint</td>
    <td>some_resource</td>
    <td>
      The designated resource identifies the SPARQL endpoint
      that this API fetches its information from.
    </td>
  </tr>

  <tr>
    <td>an API</td>
    <td>api:prefixMapping</td>
    <td>some_resource</td>
    <td>
      <code>some_resource</code> is an <code>api:PrefixMapping</code>
      element, probably written using blank nodes, giving additional
      prefixes for this configuration. The prefixes present in the
      result include the prefixes of this configuration model and
      any <code>api:prefixMapping</code>s in this configuration.
      (A result model may drop prefixes it does not use.)
    </td>
  </tr>

  <tr>
    <td>an API or endpoint</td>
    <td>elda:licence</td>
    <td>some_resource</td>
    <td>
      <code>some_resource</code> is a licence resource with arbitrary
      properties which are embedded in the metadata for a generated
      view page. The properties <code>rdfs:label</code> and
      <code>foaf:depiction</code> are recognised by the velocity
      renderer which displays them on a generated page.
    <br>
      An endpoint has all the licences of its API as well as any
      configured on the endpoint itself.
    </td>
  </tr>

  <tr>
    <td>an API or endpoint</td>
    <td>elda:notice</td>
    <td>some_resource</td>
    <td>
      <code>some_resource</code> is a notice resource with
      arbitrary properties which are embedded into the metadata
      for a generated view page.
    <br>
        An endpoint inherits the notice configured on its API.
    </td>
  </tr>

  <tr>
    <td>an api:PrefixMapping</td>
    <td>api:prefix</td>
    <td>"<em>prefix</em>"</td>
    <td>
      <em>prefix</em> is the prefix of this prefix mapping.
    </td>
  </tr>

  <tr>
    <td>an api:PrefixMapping</td>
    <td>api:namespace</td>
    <td>"<em>namespace</em>"</td>
    <td>
      <em>namespace</em> is the namespace of this prefix mapping,
      <em>i.e.</em> what the prefix represents.
    </td>
  </tr>

  <tr>
    <td>the API</td>
    <td>a</td>
    <td>api:ListEndpoint</td>
    <td>
      This endpoint is a list endpoint that can show multiple
      items selected by filters.
    </td>
  </tr>

  <tr>
    <td>any endpoint</td>
    <td>api:uriTemplate</td>
    <td style="white-space: nowrap">"/path/with/{var}"</td>
    <td>
      This endpoint is invoked when the URI path matches the template.
      The variable <code>var</code> is bound to the last segment
      of the path.
    </td>
  </tr>

  <tr>
    <td>an item endpoint</td>
    <td>a</td>
    <td>api:ItemEndpoint</td>
    <td>
      This endpoint is an item endpoint showing the single
      item specified by the associated <code>api:itemTemplate</code>.
    </td>
  </tr>

  <tr>
    <td>an API or endpoint</td>
    <td>elda:graphTemplate</td>
    <td>"URI/with/{var}"</td>
    <td>
        The SPARQL query specifies the variable-expanded form of the
        given URI as the GRAPH URI for this query. Graph templates
            attached to an API apply to all endpoints that do not have
        their own template.
    </td>
  </tr>

  <tr>
    <td>the API</td>
    <td>api:itemTemplate</td>
    <td>some_resource</td>
    <td>
      The single item displayed is the one named in <code>some_resource</code>
      with any variable references <code>{name}</code> replaced with their
      values.
    </td>
  </tr>

  <tr>
    <td>the API</td>
    <td>api:base</td>
    <td>some_resource</td>
    <td>
      If specified, all Elda's constructed URIs (such as those for different views or formats of the displayed page) will use that base URI with the path and query parameters supplied in the request. This means that a server at location A can generate URIs as though it were at location B (from which it may have been redirected).
    </td>
  </tr>

  <tr>
    <td>the API</td>
    <td>api:endpoint</td>
    <td>some_resource</td>
    <td>
      The given <code>some_resource</code> is an endpoint.
      It must be defined somewhere in this configuration.
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:variable</td>
    <td>some_resource (usually a blank node)</td>
    <td>
      Declares an API variable. The resource is usually a blank
      node with <code>rdf:type</code> <code>api:Variable</code>.
      The name, type, and value of the variable are given by
      properties of <code>some_resource</code>.
    </td>
  </tr>

  <tr>
    <td>a api:Variable</td>
    <td>api:name</td>
    <td>"<em>name</em>"</td>
    <td>
      Specifies the name of the variable.
    </td>
  </tr>

  <tr>
    <td>a api:Variable</td>
    <td>api:type</td>
    <td>some_resource</td>
    <td>
      Specifies the type of this variable. If not specified, the
      value of the variable will be a plain literal.
    </td>
  </tr>

  <tr>
    <td>a api:Variable</td>
    <td>api:value</td>
    <td>some RDF resource or literal</td>
    <td>
      Specifies the value of this variable. May be over-ridden
      by a query parameter binding.
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:maxPageSize</td>
    <td>positive integer <em>N</em></td>
    <td>
      The maximum page size for this endpoint, or the default
      for endpoints of this API, is <em>N</em>.
    </td>
  </tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:defaultPageSize</td>
    <td>positive integer <em>N</em></td>
    <td>
      The default page size for this endpoint, or the default
      for endpoints of this API, is <em>N</em>.
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:select</td>
    <td>some_resource</td>
    <td>
      The endpoint, or by default other endoints of this API,
      selects its list of items using the selector <code>some_resource</code>.
    </td>
  </tr>

  <tr>
    <td>an api:Selector</td>
    <td>api:where</td>
    <td>"<em>where clause</em>"</td>
    <td>
      This selector uses the specified <em>where clause</em> as the
      where clause in the SPARQL query to select items. Any SPARQL
      query variables <code>?var</code> that are defined as API
      variables are replaced by the value of that variable.
    </td>
  </tr>

  <tr>
    <td>an api:Selector</td>
    <td>api:filter</td>
    <td>"chain1=value1, chain2=value2..."</td>
    <td>
      The <em>chain</em>s are property chains, <i>ie</i> dot-separated
      shortnames of properties. Items are selected only if the value of
      their property chain is equal to the given value.
    </td>
  </tr>

  <tr>
    <td>an api:Selector</td>
    <td>api:parent</td>
    <td>anotherSelector</td>
    <td>
      This selector includes any filters of the given parent selector
      <code>anotherSelector</code>.
    </td>
  </tr>

  <tr>
    <td>an api:Selector</td>
    <td>api:orderBy</td>
    <td><strong>order by</strong> clause</td>
    <td>
      The selected items are sorted according to the given SPARQL
      <strong>order by</strong> clause. Note that this is useful only with
      the query parameter <strong>_where</strong> or using <strong>api:where</strong>
      in a selector.
    </td>
  </tr>

  <tr>
    <td>an api:Selector</td>
    <td>api:sort</td>
    <td>"<em>A, B...</em>"</td>
    <td>
      The selected items are sorted according to the sort
      specification, where A and B (<em>etc</em>) are property
      chains possibly preceeded by <strong>-</strong> for reverse order.
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:lang</td>
    <td>"<em>language code</em>"</td>
    <td>
      The default language for this endpoint or endpoints of this API
      are given as language codes, <em>e.g.</em> <strong>en-us</strong> or <strong>cy</strong>.
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:defaultViewer</td>
    <td>some_resource</td>
    <td>
      This endpoint, or all endpoints of this API, has as default
      viewer <code>some_resource</code>, which may be one of the
      three builtin viewers

      <ul>
        <li>
          <code>api:basicViewer</code> (show <code>rdfs:label</code>
          and <code>rdf:type</code>)
        </li>
        <li>
          <code>api:describeViewer</code> (show whatever a SPARQL
          DESCRIBE produces)
        </li>
        <li>
          <code>api:labelledDescribeViewer</code> (show DESCRIBE
          plus any labels of all resources)
        </li>
      </ul>

      or a resource (often a blank node) of type <code>api:Viewer</code>
      with <code>api:property</code> or <code>api:properties</code> properties
      (see below).
    </td>
  </tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:template</td>
    <td></td>
    <td>
    </td>
  </tr>

  <tr>
    <td>an api:Viewer</td>
    <td>api:properties</td>
    <td>"<em>chain1,chain2...</em>"</td>
    <td>
      The viewer should include properties and values for
      all of the given property chains (which may include
      the wildcard property <code>*</code>).
    </td>
  </tr>

  <tr>
    <td>an api:Viewer</td>
    <td>api:property</td>
    <td>(property1, property2 ...)</td>
    <td>
      The viewer should include a property chain with the
      given properties in that order. (Note that the properties
      are identified by their full URI or CURIE, not their
      shortname.)
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:defaultFormatter</td>
    <td>some_resource</td>
    <td>
      The default formatter of this endpoint, or all the endpoints of
      this API, is that described in the <code>api:Formatter</code>
      <code>some_resource</code>.
    </td>
  </tr>

  <tr>
    <td>api or endpoint</td>
    <td>api:formatter</td>
    <td>some_resource</td>
    <td>
      <code>some_resource</code> is a permissable formatter of this
      endpoint or all endpoints of this API.
    </td>
  </tr>

  <tr><td colspan="4"></td></tr>

  <tr>
    <td>aProperty</td>
    <td>api:structured</td>
    <td>boolean</td>
    <td>
      If the boolean is <strong>true</strong>, the JSON rendering of <code>aProperty</code>
      values will always include their datatypes and languages.
    </td>
  </tr>

</table>
