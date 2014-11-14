---
title: Elda
layout: default
landing: true
---

# Elda

Elda is a Java implementation of the [Linked Data
API](http://code.google.com/p/linked-data-api/) by 
[Epimorphics](http://epimorphics.com). Elda provides a
configurable way to create an API to access RDF data using simple RESTful 
URLs. These URLs are
translated into queries to a SPARQL endpoint. The API developer
writes an API spec &ndash; in RDF &ndash; which specifies how to
translate URLs into queries.

## Getting Elda

The currently released version of Elda is {{ site.data.version.CURRENT_RELEASE }}

* [Download the current version](http://repository.epimorphics.com/com/epimorphics/lda/elda-standalone/{{ site.data.version.CURRENT_RELEASE }}/elda-standalone-{{ site.data.version.CURRENT_RELEASE }}-exec-war.jar)
* See the [Github repository](https://github.com/epimorphics/elda) for the source code
* See [below](#maven-example) for an example Apache Maven `pom.xml` snippet

## Documentation index

* [Quick start](current/index.html)<br />
  Some
  pre-built examples which allow you to experiment with 
  the style of query and get started with building your 
  own configurations.
* [Reference](current/reference.html)<br />
  Comprehensive reference documentation for API developers
* [Velocity renderer](current/velocity.html)<br />
  ELda includes a pre-configured, but customizable, renderer
  for generating HTML pages from RDF resources using Apache Velocity.
* [Java API documentation](apidocs/)
* [Forthcoming](docs/E{{ site.data.version.FORTHCOMING_RELEASE }})<br />
  The in-development documentation, which may give an indication
  of forthcoming features or changes.

Elda tutorials

<ul>
  {% for post in site.categories.tutorial %}
    <li>
      <a href="{{ post.url | prepend: site.baseurl }}">{{ post.title }}</a>
    </li>
  {% endfor %}
</ul>


## Release history

<ul>
  {% for post in site.categories.release %}
    <li>
      <a href="{{ post.url | prepend: site.baseurl }}">{{ post.title }}</a>
      {{ post.excerpt }}
    </li>
  {% endfor %}
</ul>

## Example Maven configuration

<div id="maven-example"></div>

{% highlight xml %}
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <build>
    </build>
    <dependencies>
      <dependency>
        <groupId>com.epimorphics.lda</groupId>
        <artifactId>elda-lda</artifactId>
        <version>{{ site.data.version.CURRENT_RELEASE }}</version>
      </dependency>
      <dependency>
        <groupId>com.epimorphics.lda</groupId>
        <artifactId>elda-assets</artifactId>
        <version>{{ site.data.version.CURRENT_RELEASE }}</version>
        <type>war</type>
      </dependency>
    </dependencies>
    <repositories>
      <repository>
        <id>epi-public-repo</id>
        <name>Epimorphics Public Repository</name>
        <url>http://repository.epimorphics.com</url>
        <layout>default</layout>
        <releases>
          <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>true</enabled>
        </snapshots>
      </repository>
    </repositories>
  </project>
{% endhighlight %}
