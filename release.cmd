@echo off

if [%1]==[] goto :noversion
set VERSION=%1

if [%2]==[] (
  set FLYWAY_BRANCH=master
) else (
  set FLYWAY_BRANCH=%2
)

setlocal

SET FLYWAY_RELEASE_DIR=%cd%
SET SETTINGS_FILE=%FLYWAY_RELEASE_DIR%/settings.xml
set OSSIFY_TEST_MODE=false
set GROUP_ID=org.flywaydb.enterprise
SET RELEASE_REPOSITORY_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
SET RELEASE_REPOSITORY_ID=sonatype-nexus-staging
SET PROFILE=sonatype-release
SET FAKE_SOURCES=%FLYWAY_RELEASE_DIR%/fake-sources/flyway-sources.jar

echo ============== RELEASE START (Version: %VERSION%, Git Branch: %FLYWAY_BRANCH%)

echo ============== CLONING
git clone -b %FLYWAY_BRANCH% %FLYWAY_MAIN_REPO_URL% || goto :error

echo ============== VERSIONING MAIN
cd %FLYWAY_RELEASE_DIR%\flyway-main
call mvn versions:set -DnewVersion=%VERSION% || goto :error

echo ============== RUNNING OSSIFIER
cd %FLYWAY_RELEASE_DIR%\flyway-main\master-only\flyway-ossifier
@REM OSSifier reads the OSSIFY_TEST_MODE environment variable
call mvn clean compile exec:java -Dexec.mainClass="com.boxfuse.flyway.ossifier.OSSifier" -Dexec.args="%FLYWAY_RELEASE_DIR% %FLYWAY_RELEASE_DIR%/flyway-main" -DskipTests -DskipITs || goto :error

echo ============== PURGE ENTERPRISE
cd %FLYWAY_RELEASE_DIR%\flyway-enterprise
call mvn -s %SETTINGS_FILE% -U dependency:purge-local-repository clean install -DskipTests -DskipITs || goto :error

echo ============== PURGE COMMUNITY
cd %FLYWAY_RELEASE_DIR%\flyway
call mvn -s %SETTINGS_FILE% -U dependency:purge-local-repository clean install -DskipTests -DskipITs || goto :error

echo ============== BUILDING ENTERPRISE
cd %FLYWAY_RELEASE_DIR%\flyway-enterprise
call mvn -s %SETTINGS_FILE% -Pbuild-assemblies clean install javadoc:jar -T3 -DskipTests -DskipITs || goto :error

echo ============== BUILDING COMMUNITY
cd %FLYWAY_RELEASE_DIR%\flyway
call mvn -s %SETTINGS_FILE% -Pbuild-assemblies clean install javadoc:jar -T3 -DskipTests -DskipITs || goto :error

echo ============== BUILDING MAIN
cd %FLYWAY_RELEASE_DIR%\flyway-main
call mvn -s "%SETTINGS_FILE%" -Pbuild-assemblies -Prepo-proxy-release deploy scm:tag -DperformRelease=true -DskipTests -DskipITs || goto :error

echo ============== DEPLOYING
SET PACKAGES=flyway-core,flyway-gradle-plugin,flyway-maven-plugin,flyway-commandline,flyway-community-db-support,flyway-gcp-bigquery,flyway-gcp-spanner

echo ============== DEPLOYING COMMUNITY
cd %FLYWAY_RELEASE_DIR%\flyway
call mvn -s "%SETTINGS_FILE%" -Psonatype-release -Pbuild-assemblies deploy scm:tag -DperformRelease=true -DskipTests -DskipITs -pl %PACKAGES% -am || goto :error

echo ============== DEPLOYING ENTERPRISE TO %RELEASE_REPOSITORY_URL%
cd %FLYWAY_RELEASE_DIR%\flyway-enterprise

echo ============== DEPLOYING ENTERPRISE PARENT TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=pom.xml -DgroupId=%GROUP_ID% -DartifactId=flyway-parent -Dversion=%VERSION% -Dpackaging=pom -DupdateReleaseInfo=true -Dsources=%FAKE_SOURCES% || goto :error

echo ============== DEPLOYING ENTERPRISE CORE TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-core/target/flyway-core-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-core -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-core/target/flyway-core-%VERSION%-javadoc.jar -Dsources=%FAKE_SOURCES% -DpomFile=flyway-core/pom.xml -DupdateReleaseInfo=true -DperformRelease=true || goto :error

echo ============== DEPLOYING ENTERPRISE GCP BIGQUERY TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-gcp-bigquery/target/flyway-gcp-bigquery-%VERSION%-beta.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-gcp-bigquery -Dversion=%VERSION%-beta -Dpackaging=jar -Djavadoc=flyway-gcp-bigquery/target/flyway-gcp-bigquery-%VERSION%-beta-javadoc.jar -Dsources=%FAKE_SOURCES% -DpomFile=flyway-gcp-bigquery/pom.xml -DupdateReleaseInfo=true -DperformRelease=true || goto :error

echo ============== DEPLOYING ENTERPRISE GCP SPANNER TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-gcp-spanner/target/flyway-gcp-spanner-%VERSION%-beta.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-gcp-spanner -Dversion=%VERSION%-beta -Dpackaging=jar -Djavadoc=flyway-gcp-spanner/target/flyway-gcp-spanner-%VERSION%-beta-javadoc.jar -Dsources=%FAKE_SOURCES% -DpomFile=flyway-gcp-bigquery/pom.xml -DupdateReleaseInfo=true -DperformRelease=true || goto :error

echo ============== DEPLOYING ENTERPRISE GRADLE PLUGIN TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-gradle-plugin/target/flyway-gradle-plugin-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-gradle-plugin -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-gradle-plugin/target/flyway-gradle-plugin-%VERSION%-javadoc.jar -Dsources=%FAKE_SOURCES% -DpomFile=flyway-gradle-plugin/pom.xml -DupdateReleaseInfo=true -DperformRelease=true || goto :error

echo ============== DEPLOYING ENTERPRISE MAVEN PLUGIN TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-maven-plugin/target/flyway-maven-plugin-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-maven-plugin -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-maven-plugin/target/flyway-maven-plugin-%VERSION%-javadoc.jar -Dsources=%FAKE_SOURCES% -DpomFile=flyway-maven-plugin/pom.xml -DupdateReleaseInfo=true -DperformRelease=true || goto :error

echo ============== DEPLOYING ENTERPRISE COMMANDLINE TO %RELEASE_REPOSITORY_URL%
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%.jar -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=jar -Djavadoc=flyway-commandline/target/flyway-commandline-%VERSION%-javadoc.jar -Dsources=%FAKE_SOURCES% -DpomFile=flyway-commandline/pom.xml -DupdateReleaseInfo=true -DperformRelease=true || goto :error
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%-windows-x64.zip -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=zip -DgeneratePom=false -Dclassifier=windows-x64 -DperformRelease=true -Dsources=%FAKE_SOURCES% || goto :error
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%-linux-x64.tar.gz -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=linux-x64 -DperformRelease=true -Dsources=%FAKE_SOURCES% || goto :error
call mvn -s "%SETTINGS_FILE%" -f pom.xml gpg:sign-and-deploy-file -P%PROFILE% -DrepositoryId=%RELEASE_REPOSITORY_ID% -Durl=%RELEASE_REPOSITORY_URL% -Dfile=flyway-commandline/target/flyway-commandline-%VERSION%-macosx-x64.tar.gz -DgroupId=%GROUP_ID% -DartifactId=flyway-commandline -Dversion=%VERSION% -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=macosx-x64 -DperformRelease=true -Dsources=%FAKE_SOURCES% || goto :error

echo ============== PUBLISHING GRADLE
cd %FLYWAY_RELEASE_DIR%\gradle-plugin-publishing
call gradlew -b release-community.gradle clean publishPlugins -Dversion=%VERSION% -Dgradle.publish.key=%FLYWAY_GRADLE_KEY% -Dgradle.publish.secret=%FLYWAY_GRADLE_SECRET% || goto :error
call gradlew -b release-enterprise.gradle clean publishPlugins -Dversion=%VERSION% -Dgradle.publish.key=%FLYWAY_GRADLE_KEY% -Dgradle.publish.secret=%FLYWAY_GRADLE_SECRET% || goto :error

echo ============== RELEASE SUCCESS
cd %FLYWAY_RELEASE_DIR%
goto :EOF

:error
set ERRORLVL=%errorlevel%
echo ============== RELEASE FAILED WITH ERROR %ERRORLVL%
cd %FLYWAY_RELEASE_DIR%
pause
exit /b %ERRORLVL%

:noversion
echo ERROR: Missing version!
echo USAGE: release.cmd 1.2.3
exit /b 1
