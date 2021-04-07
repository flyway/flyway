@REM
@REM Copyright © Red Gate Software Ltd 2010-2021
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
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

if "%JAVA_ARGS%"=="" (
  set JAVA_ARGS=
)

@REM Determine Flyway edition to use
:loop
IF NOT [%1]==[] (
    IF [%1]==[-community] (
        SET FLYWAY_EDITION=community
        GOTO :loop-end
    )
    IF [%1]==[-pro] (
        SET FLYWAY_EDITION=enterprise
        GOTO :loop-end
    )
    IF [%1]==[-enterprise] (
        SET FLYWAY_EDITION=enterprise
        GOTO :loop-end
    )
    IF [%1]==[-teams] (
        SET FLYWAY_EDITION=enterprise
        GOTO :loop-end
    )
    SHIFT /1
    GOTO :loop
)
:loop-end
if "%FLYWAY_EDITION%"=="" (
  set FLYWAY_EDITION=community
)
if "%FLYWAY_EDITION%"=="pro" (
  set FLYWAY_EDITION=enterprise
)
if "%FLYWAY_EDITION%"=="teams" (
  set FLYWAY_EDITION=enterprise
)

@REM Validate the Flyway edition
set editionValid=false
for %%E in ("community" "pro" "enterprise" "teams" "community") do (
  if "%FLYWAY_EDITION%"==%%E (
    set editionValid=true
  )
)
if %editionValid%==false (
  @Echo on
  echo invalid edition "%FLYWAY_EDITION%"
  @Echo off
  EXIT /B 1
)

%JAVA_CMD% -Djava.library.path="%INSTALLDIR%\native" %JAVA_ARGS% -cp "%CLASSPATH%;%INSTALLDIR%\lib\*;%INSTALLDIR%\lib\aad\*;%INSTALLDIR%\lib\%FLYWAY_EDITION%\*;%INSTALLDIR%\drivers\*" org.flywaydb.commandline.Main %*

@REM Exit using the same code returned from Java
EXIT /B %ERRORLEVEL%