#!/bin/bash
#
# Copyright (C) 2010-2011 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Save current directory
OLDDIR=`pwd`

# Set the current directory to the installation directory
cd `dirname $0`

# Use JAVA_HOME if it is set
if [ -z $JAVA_HOME ]; then
 JAVA_CMD=java
else
 JAVA_CMD=$JAVA_HOME/bin/java
fi

$JAVA_CMD -jar bin/${project.artifactId}-${project.version}.jar $@

# Save the exit code
JAVA_EXIT_CODE=$?

# Restore current directory
cd $OLDDIR

# Exit using the same code returned from Java
exit $JAVA_EXIT_CODE
