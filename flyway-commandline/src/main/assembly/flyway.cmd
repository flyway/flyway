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

@Echo off

setlocal

@REM Set the current directory to the installation directory
set INSTALLDIR=%~dp0

if exist "%INSTALLDIR%\jre\bin\java.exe" (
 set JAVA_CMD="%INSTALLDIR%\jre\bin\java.exe"
) else (
 @REM Use JAVA_HOME if it is set
 if "%JAVA_HOME%"=="" (
  set JAVA_CMD=java
 ) else (
  set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
 )
)

SET CP=
IF DEFINED CLASSPATH ( SET CP=%CLASSPATH%;)

if "%JAVA_ARGS%"=="" (
  set JAVA_ARGS=
)

%JAVA_CMD% %JAVA_ARGS% -cp "%CP%%INSTALLDIR%\lib\*;%INSTALLDIR%\drivers\*" org.flywaydb.commandline.Main %*

@REM Exit using the same code returned from Java
EXIT /B %ERRORLEVEL%
