#!/bin/bash

if [ -z "$1" ] || [ -z "$2" ]
  then
    echo ERROR: Missing repo id or url!
    echo USAGE: deployToRemoteMavenRepo.sh my-repo-id https://myrepourl/mypath
    exit 1
fi

GROUP_ID=org.flywaydb.pro
REPO_ID=$1
REPO_URL=$2

echo ============== DEPLOYING $GROUP_ID version $VERSION artifacts TO $REPO_ID ($REPO_URL)
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-parent/pom.xml                                            -DartifactId=flyway-parent        -Dpackaging=pom    -DupdateReleaseInfo=true
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-core/flyway-core-$VERSION.jar                             -DartifactId=flyway-core          -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-core/pom.xml          -Djavadoc=flyway-core/flyway-core-$VERSION-javadoc.jar                   -Dsources=flyway-core/flyway-core-$VERSION-sources.jar
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-gradle-plugin/flyway-gradle-plugin-$VERSION.jar           -DartifactId=flyway-gradle-plugin -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-gradle-plugin/pom.xml -Djavadoc=flyway-gradle-plugin/flyway-gradle-plugin-$VERSION-javadoc.jar -Dsources=flyway-gradle-plugin/flyway-gradle-plugin-$VERSION-sources.jar
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-maven-plugin/flyway-maven-plugin-$VERSION.jar             -DartifactId=flyway-maven-plugin  -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-maven-plugin/pom.xml  -Djavadoc=flyway-maven-plugin/flyway-maven-plugin-$VERSION-javadoc.jar   -Dsources=flyway-maven-plugin/flyway-maven-plugin-$VERSION-sources.jar
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline/flyway-commandline-$VERSION.jar               -DartifactId=flyway-commandline   -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-commandline/pom.xml   -Djavadoc=flyway-commandline/flyway-commandline-$VERSION-javadoc.jar     -Dsources=flyway-commandline/flyway-commandline-$VERSION-sources.jar
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline/flyway-commandline-$VERSION-windows-x64.zip   -DartifactId=flyway-commandline   -Dpackaging=zip    -DgeneratePom=false -Dclassifier=windows-x64
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline/flyway-commandline-$VERSION-linux-x64.tar.gz  -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=linux-x64
mvn -f pom.xml deploy:deploy-file -DrepositoryId=$REPO_ID -Durl=$REPO_URL -DgroupId=$GROUP_ID -Dversion=$VERSION -Dfile=flyway-commandline/flyway-commandline-$VERSION-macosx-x64.tar.gz -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=macosx-x64


echo ============== DEPLOY SUCCESS