#!/bin/bash

export VERSION=0.9.8
#
# script to construct a delivery of elda. this should really
# be some cool maven invocation(s) but I don't know which
# ones yet ...
#
export HERE=$(pwd)

#
# clear out previous gubbins
#
rm -rf target/*
rm -rf delivery

#
# use maven to generate the webapp -- this is mostly
# just getting the right jars in the right place.

(cd ../json-rdf; mvn -Delda.version=$VERSION compile package install)
(cd ../lda;      mvn -Delda.version=$VERSION compile package install)
(cd ..;          mvn -Delda.version=$VERSION compile package install)
mvn -Delda.version=$VERSION package

#
# clone the built-in jetty distribution into 'delivery'.
#
cp -r jetty-distribution* delivery

#
# remove the clone's example webapps and contexts.
# add in the Elda contexts.
#
(cd delivery; rm -rf webapps/* context*)
cp -r contexts delivery

#
# copy the created webapp into the clone's webapps area.
# add the documentation directory in our preferred place.
#

cp -r target/elda delivery/webapps/elda
cp -r src/main/docs delivery/webapps/elda

#
# extract the unzip-and-run command class from the lda jar
#

(cd delivery; unzip $HERE/target/elda/WEB-INF/lib/lda-$VERSION.jar cmd/run.class)

#
# make the delivery area into a executable jar that runs cmd.run,
# and leave that jar in our current directory.
#

(cd delivery; jar cfe $HERE/elda-$VERSION.jar cmd.run .)

#
# done
#

echo done -- you may ship "'elda.jar'".


