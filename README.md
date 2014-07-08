<h1>Elda, an implementation of the Linked Data API</h1>

<p>
Elda is a Java implementation of the 
<a href="http://code.google.com/p/linked-data-api/" rel="nofollow">Linked Data API</a>,
which provides a configurable way to access RDF data using simple 
RESTful URLs that are translated into queries to a SPARQL endpoint. 
The API developer (probably you) writes an API spec (in RDF) which 
specifies how to translate URLs into queries. 
</p>

<p>
Elda is the Epimorphics implementation of the LDA. It comes with some pre-built  examples 
which allow you to experiment with the style of query and get started with building your own configurations. 
</p>

<p>
See the <a href="http://epimorphics.github.io/elda/docs/E1.2.34/index.html">quickstart documentation</a>.
For summaries of the latest releases, see the
<a href="http://epimorphics.github.io/elda/">documentation root</a>.
</p>

<p>
	Elda 1.2.34 was released on 8th July 2014. It allows a query to refer to
	non-default models in the SPARQL endpoint's dataset. The mappings from
	strings to resource locations for velocity templates and XSLT
	styleheets have been unified and more exactly defined.
</p>

