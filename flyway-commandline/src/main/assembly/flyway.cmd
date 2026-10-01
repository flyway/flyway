@Echo off

setlocal

@REM Set the current directory to the installation directory
set INSTALLDIR=%~dp0

if not "%FLYWAY_JAVA_CMD%"=="" (
  set JAVA_CMD=%FLYWAY_JAVA_CMD%
) else (
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
)

if "%JAVA_ARGS%"=="" (
  set JAVA_ARGS=
)
set EXTRA_ARGS=--enable-native-access=ALL-UNNAMED -Djava.library.path="%INSTALLDIR%\native"

@REM Inherit OS-level proxy settings only when explicitly enabled. Placed before JAVA_ARGS so any
@REM explicit -Dhttp.proxyHost etc. the user passes always wins (java honours the last -D wins).
if /I "%FLYWAY_USE_SYSTEM_PROXIES%"=="true" (
  set PROXY_ARGS=-Djava.net.useSystemProxies=true
) else (
  set PROXY_ARGS=
)

%JAVA_CMD% %EXTRA_ARGS% %PROXY_ARGS% %JAVA_ARGS% -cp "%CLASSPATH%;%INSTALLDIR%\lib\*;%INSTALLDIR%\lib\plugins\*;%INSTALLDIR%\lib\aad\*;%INSTALLDIR%\lib\oracle_wallet\*;%INSTALLDIR%\lib\flyway\*;%INSTALLDIR%\lib\netty\*;%INSTALLDIR%\lib\opentelemetry\*;%INSTALLDIR%\drivers\*;%INSTALLDIR%\drivers\aws\*;%INSTALLDIR%\drivers\cassandra\*;%INSTALLDIR%\drivers\couchbase\*;%INSTALLDIR%\drivers\mongo\*" org.flywaydb.commandline.Main %*

@REM Exit using the same code returned from Java
EXIT /B %ERRORLEVEL%