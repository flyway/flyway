@echo off

if [%1]==[] goto :usage
if [%2]==[] goto :usage

setlocal

set GROUP_ID=org.flywaydb.trial
set REPO_ID=%1
set REPO_URL=%2

echo ============== DEPLOYING %GROUP_ID% version %VERSION% artifacts TO %REPO_ID% (%REPO_URL%)
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-parent/pom.xml                                             -DartifactId=flyway-parent        -Dpackaging=pom    -DupdateReleaseInfo=true || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-core/flyway-core-%VERSION%.jar                             -DartifactId=flyway-core          -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-core/pom.xml          -Djavadoc=flyway-core/flyway-core-%VERSION%-javadoc.jar                   || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-gradle-plugin/flyway-gradle-plugin-%VERSION%.jar           -DartifactId=flyway-gradle-plugin -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-gradle-plugin/pom.xml -Djavadoc=flyway-gradle-plugin/flyway-gradle-plugin-%VERSION%-javadoc.jar || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-maven-plugin/flyway-maven-plugin-%VERSION%.jar             -DartifactId=flyway-maven-plugin  -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-maven-plugin/pom.xml  -Djavadoc=flyway-maven-plugin/flyway-maven-plugin-%VERSION%-javadoc.jar   || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline/flyway-commandline-%VERSION%.jar               -DartifactId=flyway-commandline   -Dpackaging=jar    -DupdateReleaseInfo=true -DpomFile=flyway-commandline/pom.xml   -Djavadoc=flyway-commandline/flyway-commandline-%VERSION%-javadoc.jar     || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline/flyway-commandline-%VERSION%-windows-x64.zip   -DartifactId=flyway-commandline   -Dpackaging=zip    -DgeneratePom=false -Dclassifier=windows-x64 || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline/flyway-commandline-%VERSION%-linux-x64.tar.gz  -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=linux-x64 || goto :error
call mvn -f pom.xml deploy:deploy-file -DrepositoryId=%REPO_ID% -Durl=%REPO_URL% -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline/flyway-commandline-%VERSION%-macosx-x64.tar.gz -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -Dclassifier=macosx-x64 || goto :error


echo ============== DEPLOY SUCCESS
goto :EOF

:error
set ERRORLVL=%errorlevel%
echo ============== DEPLOY FAILED WITH ERROR %ERRORLVL%
exit /b %ERRORLVL%

:usage
echo ERROR: Missing repo id or url!
echo USAGE: deployToRemoteMavenRepo.cmd my-repo-id https://myrepourl/mypath
exit /b 1