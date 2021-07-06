#!/usr/bin/env bash

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  # script cleanup here
  echo ============== RELEASE FAILED WITH ERROR
  cd $FLYWAY_RELEASE_DIR
}

if [ -z $1 ]; then
  echo ERROR: Missing version!
  echo USAGE: release.cmd 1.2.3
  exit 1
fi
VERSION=$1
FLYWAY_BRANCH=master
if [ ! -z $2 ]; then
  FLYWAY_BRANCH=$2
fi

FLYWAY_RELEASE_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
SETTINGS_FILE=$FLYWAY_RELEASE_DIR/settings.xml
OSSIFY_TEST_MODE=false
GROUP_ID=org.flywaydb.enterprise
RELEASE_REPOSITORY_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
RELEASE_REPOSITORY_ID=sonatype-nexus-staging
PROFILE=sonatype-release
FAKE_SOURCES=$FLYWAY_RELEASE_DIR/fake-sources/flyway-sources.jar

echo ============== RELEASE START \(Version: $VERSION, Git Branch: $FLYWAY_BRANCH\)
echo ============== CLONING
git clone -b $FLYWAY_BRANCH $FLYWAY_MAIN_REPO_URL

echo ============== VERSIONING MAIN
cd $FLYWAY_RELEASE_DIR/flyway-main
mvn versions:set -DnewVersion=$VERSION

echo ============== RUNNING OSSIFIER
cd $FLYWAY_RELEASE_DIR/flyway-main/master-only/flyway-ossifier
#OSSifier reads the OSSIFY_TEST_MODE environment variable
mvn clean compile exec:java -Dexec.mainClass="com.boxfuse.flyway.ossifier.OSSifier" -Dexec.args="$FLYWAY_RELEASE_DIR $FLYWAY_RELEASE_DIR/flyway-main" -DskipTests -DskipITs

echo ============== PURGE ENTERPRISE
cd $FLYWAY_RELEASE_DIR/flyway-enterprise
mvn -s $SETTINGS_FILE -U dependency:purge-local-repository clean install -DskipTests -DskipITs

echo ============== PURGE COMMUNITY
cd $FLYWAY_RELEASE_DIR/flyway
mvn -s $SETTINGS_FILE -U dependency:purge-local-repository clean install -DskipTests -DskipITs

echo ============== BUILDING ENTERPRISE
cd $FLYWAY_RELEASE_DIR/flyway-enterprise
mvn -s $SETTINGS_FILE -Pbuild-assemblies clean install javadoc:jar -T3 -DskipTests -DskipITs

echo ============== BUILDING COMMUNITY
cd $FLYWAY_RELEASE_DIR/flyway
mvn -s $SETTINGS_FILE -Pbuild-assemblies clean install javadoc:jar -T3 -DskipTests -DskipITs

echo ============== BUILDING MAIN
cd $FLYWAY_RELEASE_DIR/flyway-main
mvn -s "$SETTINGS_FILE" -Pbuild-assemblies -Prepo-proxy-release deploy scm:tag -DperformRelease=true -DskipTests -DskipITs

echo ============== DEPLOYING
PACKAGES=flyway-core,flyway-gradle-plugin,flyway-maven-plugin,flyway-commandline,flyway-community-db-support,flyway-gcp-bigquery,flyway-gcp-spanner

echo ============== DEPLOYING COMMUNITY
cd $FLYWAY_RELEASE_DIR/flyway
mvn -s "$SETTINGS_FILE" -Psonatype-release -Pbuild-assemblies deploy scm:tag -DperformRelease=true -DskipTests -DskipITs -pl $PACKAGES -am

echo ============== DEPLOYING ENTERPRISE TO $RELEASE_REPOSITORY_URL
cd $FLYWAY_RELEASE_DIR/flyway-enterprise

echo ============== DEPLOYING Enterprise PARENT TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=pom.xml -DgroupId=$GROUP_ID -DartifactId=flyway-parent -Dversion=$VERSION -Dpackaging=pom -DupdateReleaseInfo=true -Dsources=$FAKE_SOURCES

echo ============== DEPLOYING Enterprise CORE TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-core/target/flyway-core-$VERSION.jar -DgroupId=$GROUP_ID -DartifactId=flyway-core -Dversion=$VERSION -Dpackaging=jar -Djavadoc=flyway-core/target/flyway-core-$VERSION-javadoc.jar -Dsources=$FAKE_SOURCES -DpomFile=flyway-core/pom.xml -DupdateReleaseInfo=true -DperformRelease=true

echo ============== DEPLOYING Enterprise GCP BIGQUERY TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-gcp-bigquery/target/flyway-gcp-bigquery-$VERSION-beta.jar -DgroupId=$GROUP_ID -DartifactId=flyway-gcp-bigquery -Dversion=$VERSION-beta -Dpackaging=jar -Djavadoc=flyway-gcp-bigquery/target/flyway-gcp-bigquery-$VERSION-beta-javadoc.jar -Dsources=$FAKE_SOURCES -DpomFile=flyway-gcp-bigquery/pom.xml -DupdateReleaseInfo=true -DperformRelease=true

echo ============== DEPLOYING Enterprise GCP SPANNER TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-gcp-spanner/target/flyway-gcp-spanner-$VERSION-beta.jar -DgroupId=$GROUP_ID -DartifactId=flyway-gcp-spanner -Dversion=$VERSION-beta -Dpackaging=jar -Djavadoc=flyway-gcp-spanner/target/flyway-gcp-spanner-$VERSION-beta-javadoc.jar -Dsources=$FAKE_SOURCES -DpomFile=flyway-gcp-bigquery/pom.xml -DupdateReleaseInfo=true -DperformRelease=true

echo ============== DEPLOYING Enterprise GRADLE PLUGIN TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-gradle-plugin/target/flyway-gradle-plugin-$VERSION.jar -DgroupId=$GROUP_ID -DartifactId=flyway-gradle-plugin -Dversion=$VERSION -Dpackaging=jar -Djavadoc=flyway-gradle-plugin/target/flyway-gradle-plugin-$VERSION-javadoc.jar -Dsources=$FAKE_SOURCES -DpomFile=flyway-gradle-plugin/pom.xml -DupdateReleaseInfo=true -DperformRelease=true

echo ============== DEPLOYING Enterprise MAVEN PLUGIN TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-maven-plugin/target/flyway-maven-plugin-$VERSION.jar -DgroupId=$GROUP_ID -DartifactId=flyway-maven-plugin -Dversion=$VERSION -Dpackaging=jar -Djavadoc=flyway-maven-plugin/target/flyway-maven-plugin-$VERSION-javadoc.jar -Dsources=$FAKE_SOURCES -DpomFile=flyway-maven-plugin/pom.xml -DupdateReleaseInfo=true -DperformRelease=true

echo ============== DEPLOYING Enterprise COMMANDLINE TO $RELEASE_REPOSITORY_URL
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-$VERSION.jar -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion=$VERSION -Dpackaging=jar -Djavadoc=flyway-commandline/target/flyway-commandline-$VERSION-javadoc.jar -Dsources=$FAKE_SOURCES -DpomFile=flyway-commandline/pom.xml -DupdateReleaseInfo=true -DperformRelease=true
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-$VERSION-windows-x64.zip -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion=$VERSION -Dpackaging=zip -DgeneratePom=false -Dclassifier=windows-x64 -DperformRelease=true -Dsources=$FAKE_SOURCES
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-$VERSION-linux-x64.tar.gz -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion=$VERSION -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=linux-x64 -DperformRelease=true -Dsources=$FAKE_SOURCES
mvn -s "$SETTINGS_FILE" -f pom.xml gpg:sign-and-deploy-file -P$PROFILE -DrepositoryId=$RELEASE_REPOSITORY_ID -Durl=$RELEASE_REPOSITORY_URL -Dfile=flyway-commandline/target/flyway-commandline-$VERSION-macosx-x64.tar.gz -DgroupId=$GROUP_ID -DartifactId=flyway-commandline -Dversion=$VERSION -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=macosx-x64 -DperformRelease=true -Dsources=$FAKE_SOURCES

echo ============== PUBLISHING GRADLE
cd $FLYWAY_RELEASE_DIR/gradle-plugin-publishing
gradlew -b release-community.gradle clean publishPlugins -Dversion=$VERSION -Dgradle.publish.key=$FLYWAY_GRADLE_KEY -Dgradle.publish.secret=$FLYWAY_GRADLE_SECRET
gradlew -b release-enterprise.gradle clean publishPlugins -Dversion=$VERSION -Dgradle.publish.key=$FLYWAY_GRADLE_KEY -Dgradle.publish.secret=$FLYWAY_GRADLE_SECRET

echo ============== RELEASE SUCCESS
cd $FLYWAY_RELEASE_DIR