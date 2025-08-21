
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

if "%JAVA_ARGS%"=="" (
  set JAVA_ARGS=
)

%JAVA_CMD% -Djava.library.path="%INSTALLDIR%\native" %JAVA_ARGS% -cp "%CLASSPATH%;%INSTALLDIR%\lib\*;%INSTALLDIR%\lib\plugins\*;%INSTALLDIR%\lib\aad\*;%INSTALLDIR%\lib\oracle_wallet\*;%INSTALLDIR%\lib\flyway\*;%INSTALLDIR%\lib\netty\*;%INSTALLDIR%\lib\opentelemetry\*;%INSTALLDIR%\drivers\*;%INSTALLDIR%\drivers\aws\*;%INSTALLDIR%\drivers\gcp\*;%INSTALLDIR%\drivers\cassandra\*;%INSTALLDIR%\drivers\mongo\*" org.flywaydb.commandline.Main %*

@REM Exit using the same code returned from Java
EXIT /B %ERRORLEVEL%