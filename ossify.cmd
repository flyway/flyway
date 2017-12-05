@REM
@REM Copyright 2010-2017 Boxfuse GmbH
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

echo ============== BUILDING OSSIFIER
cd c:\Workspaces\flyway-ossifier
call mvn clean package || goto :error
echo ============== RUNNING OSSIFIER
java -jar target\flyway-ossifier-1.0-SNAPSHOT.jar

echo ============== BUILDING COMMUNITY
cd c:\Workspaces\flyway
call mvn clean install javadoc:jar -T3 || goto :error

echo ============== BUILDING PRO
cd c:\Workspaces\flyway-pro
call mvn clean install javadoc:jar -T3 || goto :error

echo ============== BUILDING ENTERPRISE
cd c:\Workspaces\flyway-enterprise
call mvn clean install javadoc:jar -T3 || goto :error

echo ============== BUILDING TRIAL
cd c:\Workspaces\flyway-trial
call mvn clean install javadoc:jar -T3 || goto :error

echo ============== SUCCESS
cd c:\Workspaces\flyway-master
pause
goto :EOF

:error
echo ============== FAILED WITH ERROR %errorlevel%
cd c:\Workspaces\flyway-master
pause
exit /b %errorlevel%