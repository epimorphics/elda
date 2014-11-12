Toggle navigation

Elda {{ site.data.version.CURRENT_RELEASE }} {.not-toc}
===========

An implementation of the linked-data API {.not-toc}
----------------------------------------

-   [Quick start](index.html)
-   [Reference](reference.html)
-   [Crib sheet](cribsheet.html)
-   [Text search example](search-example.html)
-   [Velocity renderer](velocity.html)

![Epimorphics.com](epilogo-240.png)

Table of contents {.not-toc}
-----------------

Text Search Worked Example
==========================

This document builds an example text search configuration for Elda,
based on a Fuseki snapshot for 1.0.1. It contains live links to
localhost:8080, which will only work when you're running a suitably
configured Elda.

Download the standalone jar
---------------------------

Download the latest Elda standalone jar from the Epimorphic's Maven
repository:
<http://repository.epimorphics.com/com/epimorphics/lda/elda-standalone/{{ site.data.version.CURRENT_RELEASE }}/elda-standalone-{{ site.data.version.CURRENT_RELEASE }}.jar>.
Put it somewhere handy; we'll call the directory it's in `$STANDALONE`
and use it to set up an Elda server later on. (If you already have a
server that you can configure for Elda, you might choose to use that
instead.)

Download Fuseki
---------------

Go to [fuseki snapshot
1.0.1](https://repository.apache.org/content/repositories/snapshots/org/apache/jena/jena-fuseki/1.0.1-SNAPSHOT/)
and download [the distribution
zip](https://repository.apache.org/content/repositories/snapshots/org/apache/jena/jena-fuseki/1.0.1-SNAPSHOT/jena-fuseki-1.0.1-20130914.081056-2-distribution.zip).

Unzip the distribution in a directory of your choice. **cd** into the
jena-fuseki directory. Export the name of this directory as FUSEKI.

Load example data
-----------------

Download the [example
data](https://code.google.com/p/elda/source/browse/elda-standalone/src/main/webapp/data/example-data.ttl)
from Elda's standalone jar. Copy it into \$FUSEKI.

We're going to use the supplied configuration file `config-tdb-text.ttl`
to steer the load and indexing. This sets up a dataset for holding the
example data in a TDB in the directory `DB`.

    in $FUSEKI, run:

    java -cp ./fuseki-server.jar tdb.tdbloader --tdb=config-tdb-text.ttl example-data.ttl

The config file is being used to set up the dataset, which is why it's
being supplied to the `--tdb` command parameter.

Index the data
--------------

We can use the same configuration to run the indexer:

    again in $FUSEKI, run:

     java -cp ./fuseki-server.jar jena.textindexer --desc=config-tdb-text.ttl

This time the configuration file is supplied to the --desc command
parameter. The loader set up the normal dataset; the indexer is setting
up the text-searchable dataset.

Start serving the data
----------------------

Now we can start Fuseki serving the indexed example data:

    ./fuseki-server --conf=config-tdb-text.ttl

Fuseki will serve the dataset in DB under the dataset name "/ds" on port
3030 (by default).

Now we've got Fuseki running, we can point a browser at
[](http://localhost:3030/sparql.tpl), and explore the data with SPARQL
queries, before going on to use `_search` in Elda.

Make a suitable LDA configuration
---------------------------------

Fetch the example LDA configuration file from [the stand-alone jar's
example
configurations](https://code.google.com/p/elda/source/browse/elda-standalone/src/main/webapp/specs/hello-again-world.ttl).

Comment out the line

    ; api:sparqlEndpoint <local:data/example-data.ttl>

which tells Elda that this configuration reads its data from the
webapp-relative file `data/example-data.ttl`. Comment in the line

    # ; api:sparqlEndpoint <http://localhost:3030/ds/query>

which tells Elda to query the local Fuseki we have set up above.

Save this file somewhere suitable; we'll refer to it as `$CONFIG`.

Run the standalone jar
----------------------

In `$STANDALONE`, run the standalone jar:

    java -jar elda.jar -Delda.spec=$CONFIG

which runs Elda on port 8080 using the provided configuration file. (If
port 8080 is already in use, you can change Elda's port using
`-Djetty.port=yourPortNumber`.)

Exercise \_search
-----------------

In your preferred browser, open

        http://localhost:8080/standalone/again/games

to display a list of games. The names of the games are in the example
data as objects of `rdfs:label`, and the `config-tdb-text.ttl` indexing
configuration indexes `rdfs:label` as the default field. Try searching
with

[?\_search=Age](http://localhost:8080/standalone/again/games?_search=Age)

[?\_search=Steam](http://localhost:8080/standalone/again/games?_search=Steam)

[?\_search=Industry](http://localhost:8080/standalone/again/games?_search=Industry)

[?\_search="Steam
Industry"](http://localhost:8080/standalone/again/games?_search=Steam%20Industry)

[\_search=Inventions](http://localhost:8080/standalone/again/games?_search=Inventions)

[\_search=Industry](http://localhost:8080/standalone/again/games?_search=Industry)

[\_search="Industry AND
Inventions"](http://localhost:8080/standalone/again/games?_search=Industry%20AND%20Inventions)

[\_search="Industry
Inventions"](http://localhost:8080/standalone/again/games?_search=Industry%20Inventions)

appended to the games URI above.

Wrapup
------

You should now be in a position to work with data of your own choosing
and to experiment (if necessary) with different query configurations for
different Elda endpoints.

* * * * *

© Copyright 2011-2013 Epimorphics Limited. For licencing conditions see
<http://http://epimorphics.github.io/elda/LICENCE.html>.

