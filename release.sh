#!/usr/bin/env bash

set -Eeo pipefail
trap 'onError $?' ERR

onError() {
  echo ============== RELEASE FAILED WITH ERROR "$1"
  cd "$FLYWAY_RELEASE_DIR"
  exit "$1"
}

if [ -z "$1" ]; then
  echo ERROR: Missing version!
  echo USAGE: release.cmd 1.2.3
  exit 1
fi
VERSION=$1
FLYWAY_BRANCH=master
if [ -n "$2" ]; then
  FLYWAY_BRANCH=$2
fi

FLYWAY_BETA=""
if [ -n "$3" ]; then
  FLYWAY_BETA=$3
fi

FLYWAY_RELEASE_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
SETTINGS_FILE=$FLYWAY_RELEASE_DIR/settings.xml
OSSIFY_TEST_MODE=false
GROUP_ID=org.flywaydb.enterprise
RELEASE_REPOSITORY_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
RELEASE_REPOSITORY_ID=sonatype-nexus-staging
PROFILE=sonatype-release
FAKE_SOURCES=$FLYWAY_RELEASE_DIR/fake-sources/flyway-sources.jar
QUALIFIER=-beta

echo ============== RELEASE START \(Version: "$VERSION", Git Branch: "$FLYWAY_BRANCH"\)
echo ============== CLONING
git clone -b "$FLYWAY_BRANCH" "$FLYWAY_MAIN_REPO_URL"

echo ============== VERSIONING MAIN
cd "$FLYWAY_RELEASE_DIR"/flyway-main
mvn versions:set -DnewVersion="$VERSION"
if [ -n "$FLYWAY_BETA" ]; then
  for i in ${FLYWAY_BETA//,/ }; do
    mvn versions:set -DnewVersion="$VERSION"$QUALIFIER -pl "$i"
  done
fi

echo ============== VERSIONING BETA DEPENDENCIES
cd "$FLYWAY_RELEASE_DIR"/flyway-main
if [ -n "$FLYWAY_BETA" ]; then
  for i in ${FLYWAY_BETA//,/ }; do
    mvn versions:set-property -Dproperty="$i.version" -DnewVersion="$VERSION"$QUALIFIER -pl flyway-commandline
  done
fi

echo ============== RUNNING OSSIFIER
cd "$FLYWAY_RELEASE_DIR"/flyway-main/master-only/flyway-ossifier
#OSSifier reads the OSSIFY_TEST_MODE environment variable
if [[ "$TEAMCITY_OS" == *"Windows"* ]]; then
  WIN_FLYWAY_RELEASE_DIR=$(cygpath -w ${FLYWAY_RELEASE_DIR})
  mvn clean compile exec:java -Dexec.mainClass="com.boxfuse.flyway.ossifier.OSSifier" -Dexec.args="$WIN_FLYWAY_RELEASE_DIR $WIN_FLYWAY_RELEASE_DIR/flyway-main" -DskipTests -DskipITs
else
  mvn clean compile exec:java -Dexec.mainClass="com.boxfuse.flyway.ossifier.OSSifier" -Dexec.args="$FLYWAY_RELEASE_DIR $FLYWAY_RELEASE_DIR/flyway-main" -DskipTests -DskipITs
fi

echo ============== PURGE ENTERPRISE
cd "$FLYWAY_RELEASE_DIR"/flyway-enterprise
mvn -s "$SETTINGS_FILE" -U dependency:purge-local-repository clean install -DskipTests -DskipITs

echo ============== PURGE COMMUNITY
cd "$FLYWAY_RELEASE_DIR"/flyway
mvn -s "$SETTINGS_FILE" -U dependency:purge-local-repository clean install -DskipTests -DskipITs

echo ============== BUILDING ENTERPRISE
cd "$FLYWAY_RELEASE_DIR"/flyway-enterprise
mvn -s "$SETTINGS_FILE" -Pbuild-assemblies clean install javadoc:jar -T3 -DskipTests -DskipITs

echo ============== BUILDING COMMUNITY
cd "$FLYWAY_RELEASE_DIR"/flyway
mvn -s "$SETTINGS_FILE" -Pbuild-assemblies clean install javadoc:jar -T3 -DskipTests -DskipITs

echo ============== BUILDING MAIN
cd "$FLYWAY_RELEASE_DIR"/flyway-main
mvn -s "$SETTINGS_FILE" -Pbuild-assemblies -Prepo-proxy-release deploy scm:tag -DperformRelease=true -DskipTests -DskipITs

echo ============== DEPLOYING
PACKAGES=flyway-core,flyway-gradle-plugin,flyway-maven-plugin,flyway-commandline,flyway-community-db-support,flyway-gcp-bigquery,flyway-gcp-spanner

echo ============== DEPLOYING COMMUNITY
cd "$FLYWAY_RELEASE_DIR"/flyway
mvn -s "$SETTINGS_FILE" -Psonatype-release -Pbuild-assemblies deploy scm:tag -DperformRelease=true -DskipTests -DskipITs -pl $PACKAGES -am

echo ============== DEPLOYING ENTERPRISE TO $RELEASE_REPOSITORY_URL
cd "$FLYWAY_RELEASE_DIR"/flyway-enterprise

echo ============== DEPLOYING Enterprise PARENT TO "$RELEASE_REPOSITORY_URL"
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=pom.xml -DgroupId=$GROUP_ID -DartifactId=flyway-parent -Dversion="$VERSION" -Dpackaging=pom -DupdateReleaseInfo=true -Dsources="$FAKE_SOURCES"
for i in ${PACKAGES//,/ }; do
  NAME=${i^^}
  NAME=${NAME//-/ }
  echo ============== DEPLOYING Enterprise "$NAME" TO "$RELEASE_REPOSITORY_URL"
  NEWVERSION=$VERSION
  if [ -f "$i"/target/"$i"-"$VERSION""$QUALIFIER".jar ]; then
    NEWVERSION=$VERSION$QUALIFIER
  fi
  mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile="$i"/target/"$i"-"$NEWVERSION".jar -DgroupId=$GROUP_ID -DartifactId="$i" -Dversion="$NEWVERSION" -Dpackaging=jar -Djavadoc="$i"/target/"$i"-"$NEWVERSION"-javadoc.jar -Dsources="$FAKE_SOURCES" -DpomFile="$i"/pom.xml -DupdateReleaseInfo=true -DperformRelease=true
  if [ "$i" == "flyway-commandline" ]; then
    mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-"$NEWVERSION"-windows-x64.zip -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion="$NEWVERSION" -Dpackaging=zip -DgeneratePom=false -Dclassifier=windows-x64 -DperformRelease=true -Dsources="$FAKE_SOURCES"
    mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-"$NEWVERSION"-linux-x64.tar.gz -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion="$NEWVERSION" -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=linux-x64 -DperformRelease=true -Dsources="$FAKE_SOURCES"
    mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-"$NEWVERSION"-macosx-x64.tar.gz -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion="$NEWVERSION" -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=macosx-x64 -DperformRelease=true -Dsources="$FAKE_SOURCES"
  fi
done

echo ============== PUBLISHING GRADLE
cd "$FLYWAY_RELEASE_DIR"/gradle-plugin-publishing
gradlew -b release-community.gradle clean publishPlugins -Dversion="$VERSION" -Dgradle.publish.key="$FLYWAY_GRADLE_KEY" -Dgradle.publish.secret="$FLYWAY_GRADLE_SECRET"
gradlew -b release-enterprise.gradle clean publishPlugins -Dversion="$VERSION" -Dgradle.publish.key="$FLYWAY_GRADLE_KEY" -Dgradle.publish.secret="$FLYWAY_GRADLE_SECRET"

echo ============== RELEASE SUCCESS
cd "$FLYWAY_RELEASE_DIR"
