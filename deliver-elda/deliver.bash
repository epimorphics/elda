#!/bin/bash

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

mvn package

#
# clone the built-in jetty distribution into 'delivery'.
#
cp -r jetty-distribution* delivery

#
# remove the clone's example webapps and contexts.
#
(cd delivery; rm -rf webapps/* context*)
cp -r contexts delivery

#
# copy the created webapp into the clone's webapps area.
#

cp -r target/elda delivery/webapps/elda

#
# extract the unzip-and-run command class from the lda jar
#

(cd delivery; unzip $HERE/target/elda/WEB-INF/lib/lda-0.0.1-SNAPSHOT.jar cmd/run.class)

#
# make the delivery area into a executable jar that runs cmd.run,
# and leave that jar in our current directory.
#

(cd delivery; jar cfe $HERE/elda.jar cmd.run .)

#
# done
#

echo done -- you may ship "'elda.jar'".


