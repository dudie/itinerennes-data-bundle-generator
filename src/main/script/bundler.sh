#!/bin/bash

###
# [license]
# Itinerennes data resources generator
# ~~~~
# Copyright (C) 2013 - 2014 Dudie
# ~~~~
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as
# published by the Free Software Foundation, either version 3 of the 
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public 
# License along with this program.  If not, see
# <http://www.gnu.org/licenses/gpl-3.0.html>.
# [/license]
###

fail() {
  echo $2
  exit $1
}

echo "Using JAVA_HOME=$JAVA_HOME"
echo "Using JAVA_OPTS=$JAVA_OPTS"

JAVA=$JAVA_HOME/bin/java
test -f $JAVA || fail 1 "java not found, check JAVA_HOME"

$JAVA $JAVA_OPTS -jar $(dirname $0)/${project.artifactId}-${project.version}.jar $*
