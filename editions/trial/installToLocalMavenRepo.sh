#!/bin/bash

GROUP_ID=org.flywaydb.trial

echo ============== INSTALLING $GROUP_ID version $VERSION artifacts in your local Maven repository

mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-parent\pom.xml                                            -DartifactId=flyway-parent        -Dpackaging=pom
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-core\flyway-core-$VERSION.jar                             -DartifactId=flyway-core          -Dpackaging=jar    -DpomFile=flyway-core\pom.xml          -Djavadoc=flyway-core\flyway-core-$VERSION-javadoc.jar
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-gradle-plugin\flyway-gradle-plugin-$VERSION.jar           -DartifactId=flyway-gradle-plugin -Dpackaging=jar    -DpomFile=flyway-gradle-plugin\pom.xml -Djavadoc=flyway-gradle-plugin\flyway-gradle-plugin-$VERSION-javadoc.jar
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-maven-plugin\flyway-maven-plugin-$VERSION.jar             -DartifactId=flyway-maven-plugin  -Dpackaging=jar    -DpomFile=flyway-maven-plugin\pom.xml  -Djavadoc=flyway-maven-plugin\flyway-maven-plugin-$VERSION-javadoc.jar
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline\flyway-commandline-$VERSION.jar               -DartifactId=flyway-commandline   -Dpackaging=jar    -DpomFile=flyway-commandline\pom.xml   -Djavadoc=flyway-commandline\flyway-commandline-$VERSION-javadoc.jar
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline\flyway-commandline-$VERSION.zip               -DartifactId=flyway-commandline   -Dpackaging=zip    -DgeneratePom=false
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline\flyway-commandline-$VERSION.tar.gz            -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline\flyway-commandline-$VERSION-windows-x64.zip   -DartifactId=flyway-commandline   -Dpackaging=zip    -DgeneratePom=false -classifier=windows-x64
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline\flyway-commandline-$VERSION-linux-x64.tar.gz  -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -classifier=linux-x64
mvn install:install-file -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline\flyway-commandline-$VERSION-macosx-x64.tar.gz -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -classifier=macosx-x64

echo ============== INSTALL SUCCESS