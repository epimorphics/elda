#!/bin/bash

export VERSION=$(grep "<version>" ../pom.xml | head -n 1 | sed -e 's/ *<version>//' -e 's:</version>::')

echo Extracted version number is $VERSION

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
# ensure top-level version number is pushed into modules.
# then do a build. this will sort out the webapp jars here.
#

(cd ..; mvn -N versions:update-child-modules)
(cd ..; mvn clean compile package install)

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

echo done -- you may ship "'elda-$VERSION.jar'".


