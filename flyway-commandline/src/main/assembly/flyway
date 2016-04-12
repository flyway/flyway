#!/usr/bin/env bash
#
# Copyright (C) 2010-2015 Axel Fontaine
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

# Detect cygwin
cygwin=false
linux=false
case "`uname`" in
  CYGWIN*|MINGW*) cygwin=true;;
  Linux*) linux=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Detect the installation directory
INSTALLDIR=`dirname "$PRG"`

if [ -x "$INSTALLDIR/jre/bin/java" ]; then
 JAVA_CMD=$INSTALLDIR/jre/bin/java
else
 # Use JAVA_HOME if it is set
 if [ -z "$JAVA_HOME" ]; then
  JAVA_CMD=java
 else
  JAVA_CMD=$JAVA_HOME/bin/java
 fi
fi

JAVA_ARGS=
CP="$INSTALLDIR/lib/*:$INSTALLDIR/drivers/*"

if $linux; then
  JAVA_ARGS=-Djava.security.egd=file:/dev/../dev/urandom
fi

if $cygwin; then
  CP=$(cygpath -pw "$CP")
fi

"$JAVA_CMD" $JAVA_ARGS -cp "$CP" org.flywaydb.commandline.Main "$@"

# Exit using the same code returned from Java
exit $?
