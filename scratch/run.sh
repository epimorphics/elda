#!/bin/bash

rm -f wgetlog

for data in *.cases; do
    cat $data | while read x
    do
      export name=$(echo $x | sed -e 's/;.*//')
      export type=$(echo $x | sed -e 's/[^;]*;//' -e 's/;.*//')
      export url=$(echo $x | sed -e 's/[^;]*;[^;]*;//')
      echo doing $name "("$type")"
      wget -O got/$name $url 2>>wgetlog
      if [ -a gold/$name ] ; then
        if diff got/$name gold/$name ; then
            echo "    " PASSED
        else
            echo "    " FAILED textual identity. Have to do something $type clever.
        fi
      else
        echo "    " CREATED new golden result for $name
        mv got/$name gold/$name
      fi 
    
    done
done
