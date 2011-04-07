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
(cd delivery \
    ; mv LICENSE-APACHE-2.0.txt jetty-LICENSE-APACHE-2.0.txt \
    ; mv LICENSE-ECLIPSE-1.0.html jetty-LICENSE-ECLIPSE-1.0.html \
    ; mv javadoc jetty-javadoc  \
    ; mv README.txt jetty-README.txt  \
    ; mv VERSION.txt jetty-VERSION.txt \
    ; mv notice.html jetty-notice.html \
    ; mv about.html jetty-about.html \
)
cp ../LICENCE delivery/elda-LICENCE
cp ../LICENCE.html delivery/elda-LICENCE.html
cp ../README-demo.text delivery/elda-README-demo.text
cp ../ReleaseNotes.text delivery/elda-ReleaseNotes.text

#
# put in the existing javadoc. First we need to construct it (I can't make 
# `mvn javadoc:aggregate` work)
#
(cd ..; \
  pwd ; \
  rm -rf all-javadoc ; \
  javadoc -d all-javadoc 2>javadoc.issues \
   -sourcepath lda/src/main/java:json-rdf/src/main/java \
   -subpackages com \
   -classpath /home/christopher/M2/repository/junit/junit/4.8.2:$(echo ./deliver-elda/target/elda/WEB-INF/lib/*jar | sed -e 's/ /:/g')/ ; \
  rm -rf deliver-elda/delivery/elda-javadoc ; \
  mv all-javadoc deliver-elda/delivery/elda-javadoc \
)

#
# remove the clone's example webapps and contexts.
#
(cd delivery; rm -rf webapps/* context*)

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


