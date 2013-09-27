#!/bin/bash

fail() {
  echo $2
  exit $1
}

echo "Using JAVA_HOME=$JAVA_HOME"

JAVA=$JAVA_HOME/bin/java
test -f $JAVA || fail 1 "java not found, check JAVA_HOME"

$JAVA -jar $(dirname $0)/${project.artifactId}-${project.version}.jar $*