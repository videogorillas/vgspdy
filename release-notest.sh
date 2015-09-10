#!/bin/bash

mvn -Dmaven.test.skip=true -DperformRelease clean compile jar:jar source:jar install
