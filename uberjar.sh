#!/bin/bash

set -xue

mkdir target/uberjar || true
root=`pwd`
cd $root/target/uberjar
for each in $root/target/dependency/*jar ;do
    jar xvf $each
done
rm -fr $root/target/uberjar/META-INF
jar cvf $root/jetty-spdy-9.3.0-SNAPSHOT-uberjar.jar .

