---
title: Rendering with Velocity
layout: default-toc
---

# Rendering Elda results with Velocity templates

Elda uses a *renderer* to generate output from the set of RDF resources that are selected by the input URL. Starting with version 1.3.0, Elda uses by default a renderer based on [Apache Velocity](http://velocity.apache.org) to generate HTML output. This renderer is a complete re-write of the older, provisional Velocity renderer, and is not backwards compatible with the previous version.

In the remainder of this guide, we will show you:

* [how to set-up and configure the Velocity renderer in the configuration code](#config-use-velocity);
* how to customise the sytlesheets to change the look and feel of the rendered output;
* how to customise the Velocity macros to change specific renderering behaviours;
* what's available in the API provide to Velocity template developers.

## What does a renderer do?

Elda uses the information from the URL, together with the configuration file, to generate SPARQL queries to the underlying triple store and select a set of RDF resource and their descriptions (i.e. sets of RDF properties). This resultset is returned to the requester in some format: JSON, XML, Turtle or an HTML web page. A *renderer* is the part of Elda that turns the resultset into the output format. The HTML renderer does that job to display HTML web pages. In previous versions of Elda, the default HTMl renderer used XSLT to convert the resultset to HTML. That renderer is still included in Elda, but since version 1.3.0 we have switch to using Velocity as the default HTML renderer.

Here is an example of a site that uses Velocity to render HTML pages:

![]({{ "/images/velocity-screenshot-1.png" | prepend: site.baseurl }})

# <a name="config-use-velocity"></a>Configuring and using the Velocity renderer

To use the Velocity renderer to generate HTML, attach this formatter to
your API spec:

{% highlight html linenos %}
<yourSpec> api:defaultFormatter
  [a elda:VelocityFormatter ;
   api:name "html" ;
   api:mimeType "text/html" ;
   elda:className "com.epimorphics.lda.renderers.VelocityRendererFactory" 
  ]
{% endhighlight %}

This will do two things: it will create an instance of a `VelocityRendering` each time a resultset needs to be rendered into HTML, and it will look for a template file (and associated assets) to turn the `VelocityRendering` into HTML output. The root template file is `index.vm`. Other than the name, the renderer makes no assumptions about the root template, or what it does with the information in the `VelocityRendering`.  A standard template is provided in the *Elda assets* project &ndash; you are free to use this standard template as-is, adapt it to your needs, or ignore it and create your own Velocity templates to meet your needs precisely.  Guidance for customising the standard template appear below.

### What's the difference between the VelocityRendering and the template?

Rendering a resultset into HTML using Elda's Velocity renderer goes through two stages. In the first, a `VelocityRendering` object is created. This stage has two goals:

* to create a set of Java accessor objects, which provide a convenience API onto the elements of the resultset so that the template developer's job is easier. For example, an Elda resultset frequently split up into manageable units that the LDA calls *pages* (the first 10 results, the second 10, etc). Page information is recorded into the RDF statements in the LDA resultset. To make this easier to use in a template, we provide a Java object `com.epimorphics.lda.renderers.common.Page`. The `Page` object has direct API calls, such as `pageNumber()`, `itemsPerPage()` to make it easy to incorporate the information from the page description in RDF into a template
* to unwind the graph into a tree-structure, suitable for display in an HTML document model or other tree-structured output notation.

Both of these jobs *could* have been achieved in the Velocity template language, VTL. However, VTL has some limitations in terms of performance and expressiveness, that make this harder. Moreover, by writing the `VelocityRendering` objects in Java, it becomes much easier to write unit tests to ensure correct, stable behaviour over time.

### Configuration options

The Velocity renderer uses three `api:variable`s in the configuration to specify non-default values for configuration options:

* `velocityTemplate` for the name of the root template file (default: `index.vm`)
* `_resourceRoot` for a path that should be prepended to assets, such as CSS stylesheets and JavaScript files
* `_velocityRoot` for a path on the server's file system where the Velocity templates are found.

#### Example

Suppose you unpack the files from *Elda assets* into `/var/www/elda/assets`, and that you arrange that all requests coming in to *myEldaApp* are forwarded from the front-end Apache or Nginx server to a Tomcat webapp container hosting Elda. We need to supply both of these pieces of information to the Elda config, so that the dynamically generated pages will contain the correct links:

{% highlight html linenos %}
<myEldaAppSpec> api:defaultFormatter
  [a elda:VelocityFormatter ;
   api:name "html" ;
   api:mimeType "text/html" ;
   elda:className "com.epimorphics.lda.renderers.VelocityRendererFactory" ;
   api:variable [
     api:name "_resourceRoot"; 
     api:value "/myEldaApp"
   ];
   api:variable [
     api:name "_velocityRoot"; 
     api:value "/var/www/elda/assets"
   ]
  ]
{% endhighlight %}

Now suppose that we would like to override some of the default behaviours of the Velocity renderer. Velocity's behaviour is to use the first definition of a macro or partial that it finds, so our overrides have to be found before the default files. We decide to put our locally customised assets into `/var/www/myEldaApp/assets`:

{% highlight html linenos %}
<myEldaAppSpec> api:defaultFormatter
  [a elda:VelocityFormatter ;
   api:name "html" ;
   api:mimeType "text/html" ;
   elda:className "com.epimorphics.lda.renderers.VelocityRendererFactory" ;
   api:variable [
     api:name "_resourceRoot"; 
     api:value "/myEldaApp"
   ];
   api:variable [
     api:name "_velocityRoot"; 
     api:value "/var/www/myEldaApp/assets, /var/www/elda/assets"
   ]
  ]
{% endhighlight %}

**Other notes **

* You can choose to specify a Velocity formatter as a property of an
endpoint rather than as the API-wide default.
* You can change the associated suffix of the formatter by changing the
value of its `api:name` property, and change the content-type of the
generated page by changing the value of the `api:mimeType` property.

### Velocity properties

Velocity uses a set of [configuration properties](http://velocity.apache.org/engine/releases/velocity-1.5/developer-guide.html#velocity_configuration_keys_and_values) to control aspects of its internal behaviour. By default, we leave all of these properties with their default values, execpt that `file.resource.loader.path` is set to the path given by `_velocityRoot`. However, the renderer will look for a `velocity.properties` file in the loader path, so you can override any of the default Velocity configuration properties by creating a `velocity.properties` file with appropriate keys and values. For example:

{% highlight properties linenos %}
input.encoding = UTF-8
output.encoding = UTF-8
{% endhighlight %}

# <a name="customising-velocity-renderer"></a>Customising the Velocity HTML renderer

The built-in renderer's stylesheets and Velocity templates are intended to be a complete out-of-the-box solution for generating usable HTML pages from Elda results. However, in many cases it's desirable to customise the output. Common examples include: changing the colour scheme to fit the look and feel of an existing site,  and extending special behaviours to display certain kinds of RDF data in content-specific ways. We cover both of these cases below.

## <a name="customising-look-and-feel"></a>Customising the CSS stylesheets

All of the styling information for the HTML pages generated by the Velocity templates is handled by CSS stylesheets. Following best practice for CSS, there are no [inline styles](http://www.w3.org/wiki/CSS_basics#Inline_styles) used in the generated pages.

In the *Elda assets* project, all of the relevant files can be found in the `src/main/webapp` directory (this follows the Apache Maven standard layout for web application projects). Within this directory, all of the files relevant to the Velocity renderer are in the `velocity/` directory, and the stylesheets are in `velocity/css`. In here, you will find styles related to various elements of the standard layout generated by Elda's Velocity renderer:

* `bootstrap.min.css`, `bootstrap-theme.min.css` <br /> By default, Elda's HTML pages use the [Bootstrap](http://getbootstrap.com) responsive design framework. Other Bootstrap themes can be downloaded from the internet.
* `font-awesome.min.css` <br /> Provides a collection of [useful icon glyphs](http://fortawesome.github.io/Font-Awesome/)
* `codemirror.css`, `fold-gutter.css`, `jquery.datatables.min.css`, `qonsole.css` <br /> Used to support the SPARQL editing console used to view and run example SPARQL queries provided by Elda
* `site.css` <br /> Elda-specific styles

In general, `site.css` is first place to start when seeking to change the look and feel of Elda generated pages, although selecting a different Bootstrap theme might also be a good initial step for more radical changes.

### Example: changing the basic colour scheme

Elda's default stylesheet uses a dark blue as a base colour. Let's suppose that we want to change that to use an amber theme. Here are sample before and after screenshots of the change we want to make:

<figure>
  <img src="{{ "/images/velocity-screenshot-3.png" | prepend: site.baseurl }}"
       alt="Before and after changing resource colours"></img>
<figcaption>Figure 1: Before and after changing resource colours in `site.css`</figcaption>
</figure>

Here is the CSS needed to change the colours of the outer and nested RDF resource labels, and the top navigation bar:

{% highlight css linenos %}
/* Colours for outermost resource labels */
h1.resource-label {
  background-color: #ffc107;
  color: #111;
}
section {
  border-color: #ffe082;
}

/* Colours for nested resource labels */
h3.resource-label {
  background-color: rgb( 255, 193, 7 );
  background-color: rgba(255, 193, 7, 0.8);
  color: #111;
}
.nested {
  border-color: #ffe082;
}

/* Navigation bar colours */
.navbar-default {
  background: linear-gradient(#ff6f00, #ffb300 80%, rgba(256, 256, 256, 0.3)) repeat scroll 0 0 rgba(0, 0, 0, 0);
}

.navbar h1 {
  font-size: 18pt;
  margin-top: 0;
  padding-top: 10px;
}

.navbar h1 a {
  color: white;
  vertical-align: top;
  font-weight: bold;
}
{% endhighlight %}


These CSS rules can simply be added to the `site.css` stylesheet that is deployed on your system. However, a disadvantage of this approach is that unpacking a new release of *Elda assets* onto your system risks overwriting your changes. Conversely, never updating *Elda assets* means that you risk not benefitting from the latest updates. A more robust method of incorporating the changed stylesheet can be achieved with a small change to the Velocity templates, as shown below.

## <a name="customising-velocity-templates"></a>Customising the Velocity templates



*********************
*********************

Names in the Velocity context
-----------------------------

When rendering a page using Velocity, Elda binds several names in the
context:

name

value

thisPage

A WrappedNode for a resource with the URI of this page.

isItemEndpoint

A boolean, true iff this page is for an item endpoint.

isListEndpoint

A boolean, true iff this page is for a list endpoint.

primaryTopic

A WrappedNode for the primary topic of this page, only defined if this
page is for an item endpoint.

names

a map from resources to their short names, passed when needed to methods
on `WrappedNode`s.

formats

a list of Format objects in order of their names. Each Format has a
getName and getLink method; a format's Link is the URI needed to fetch
the version of the current page in this format.

items

the list of selected items. Each item is a `WrappedNode`.

meta

A map from string pathnames to `WrappedNode` values. Each pathname is
the dot-separated concatenation of the short names of the properties in
the path. The proeprty chains are those of the metadata in the result
model, disregarding the `termBinding`s and the `variableValues` since
these have their own context variables.

vars

A map from LDA variable names to `WrappedNode`s representing their
values.

filters

A map of the name=parameter query parameter values (discarding all the
ones that start with "\_").

ids

An IdMap object which contains a map from Resources to their identifiers
for use in HTML `id=` attributes.

utils

Utility methods not appropriate for placing in WrappedNode or
WrappedString.

utils.println(Object)

One-argument println to System.err, as a debugging aid. Output starts
with "\>\>".

utils.println(Object, Object)

Two-argument space-separated println to System.err, as a debugging aid.
Output starts with "\>\>".

utils.sort(Object c)

Sorts the sortable collection c in-place.

utils.join(Collection c, String infix)

Return an infix-separated concatenation of the toString() values of the
elements of c.

utils.currentMillis()

Returns the current time in milliseconds since the Velocity renderer was
initialised for this rendering, meant as a debugging/profiling aid.

utils

Wrapped RDFNodes
----------------

WrappedNodes are wrappers round Jena RDFNodes. When they are created,
they are given a ShortNames object to allow them to render their short
names and an IdMap object to hold their allocated Id. They have the
following methods:

  -------------------------------------------------------------------------
  signature
  description
  ------------------------------------ ------------------------------------
  boolean equals(Object other)         String getId()
  A WrappedNode is .equals to another  Answer the id of this WrappedNode,
  object if that object is a           allocating a fresh one if necessary.
  WrappedNode and their underlying     
  RDFNodes are equal.                  
  -------------------------------------------------------------------------

Where a String result might contain HTML-significant characters,
WrappedNodes return a WrappedString object.

  -------------------------------------------------------------------------
  signature
  description
  ------------------------------------ ------------------------------------
  WrappedString cut()                  String toString()
  Returns a new wrapped string who's   Return the content of this
  content is the content of this       WrappedString, performing HTML
  wrapped string but with spaces       escaping.
  inserted in place of runs of '\_'    
  and between a lower-case letter      
  followed by an upper-case one, with  
  that letter converted to lower-case. 
  -------------------------------------------------------------------------

