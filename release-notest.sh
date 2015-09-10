#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-6-openjdk-amd64/
export PATH=/usr/lib/jvm/java-6-openjdk-amd64/bin:$PATH

mvn -Dmaven.test.skip=true -DperformRelease clean compile jar:jar source:jar install
