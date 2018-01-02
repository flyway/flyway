@REM
@REM Copyright 2010-2018 Boxfuse GmbH
@REM
@REM INTERNAL RELEASE. ALL RIGHTS RESERVED.
@REM
@REM Must
@REM be
@REM exactly
@REM 13 lines
@REM to match
@REM community
@REM edition
@REM license
@REM length.
@REM

@echo off

setlocal

SET GROUP_ID=org.flywaydb.enterprise

echo ============== INSTALLING %GROUP_ID% version %VERSION% artifacts in your local Maven repository

call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-parent\pom.xml                                             -DartifactId=flyway-parent        -Dpackaging=pom || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-core\flyway-core-%VERSION%.jar                             -DartifactId=flyway-core          -Dpackaging=jar    -DpomFile=flyway-core\pom.xml          -Djavadoc=flyway-core\flyway-core-%VERSION%-javadoc.jar                   -Dsources=flyway-core\flyway-core-%VERSION%-sources.jar || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-gradle-plugin\flyway-gradle-plugin-%VERSION%.jar           -DartifactId=flyway-gradle-plugin -Dpackaging=jar    -DpomFile=flyway-gradle-plugin\pom.xml -Djavadoc=flyway-gradle-plugin\flyway-gradle-plugin-%VERSION%-javadoc.jar -Dsources=flyway-gradle-plugin\flyway-gradle-plugin-%VERSION%-sources.jar || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-maven-plugin\flyway-maven-plugin-%VERSION%.jar             -DartifactId=flyway-maven-plugin  -Dpackaging=jar    -DpomFile=flyway-maven-plugin\pom.xml  -Djavadoc=flyway-maven-plugin\flyway-maven-plugin-%VERSION%-javadoc.jar   -Dsources=flyway-maven-plugin\flyway-maven-plugin-%VERSION%-sources.jar || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline\flyway-commandline-%VERSION%.jar               -DartifactId=flyway-commandline   -Dpackaging=jar    -DpomFile=flyway-commandline\pom.xml   -Djavadoc=flyway-commandline\flyway-commandline-%VERSION%-javadoc.jar     -Dsources=flyway-commandline\flyway-commandline-%VERSION%-sources.jar || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline\flyway-commandline-%VERSION%.zip               -DartifactId=flyway-commandline   -Dpackaging=zip    -DgeneratePom=false || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline\flyway-commandline-%VERSION%.tar.gz            -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline\flyway-commandline-%VERSION%-windows-x64.zip   -DartifactId=flyway-commandline   -Dpackaging=zip    -DgeneratePom=false -classifier=windows-x64 || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline\flyway-commandline-%VERSION%-linux-x64.tar.gz  -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -classifier=linux-x64 || goto :error
call mvn install:install-file -DgroupId=%GROUP_ID% -Dversion=%VERSION% -Dfile=flyway-commandline\flyway-commandline-%VERSION%-macosx-x64.tar.gz -DartifactId=flyway-commandline   -Dpackaging=tar.gz -DgeneratePom=false -classifier=macosx-x64 || goto :error

echo ============== INSTALL SUCCESS
goto :EOF

:error
set ERRORLVL=%errorlevel%
echo ============== INSTALL FAILED WITH ERROR %ERRORLVL%
exit /b %ERRORLVL%