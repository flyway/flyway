@REM
@REM Copyright 2010-2014 Axel Fontaine
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

@REM Save current directory
set OLDDIR=%CD%

@REM Set the current directory to the installation directory
chdir /d %~dp0

@REM Use JAVA_HOME if it is set
if "%JAVA_HOME%"=="" (
 set JAVA_CMD=java
) else (
 set JAVA_CMD="%JAVA_HOME%\bin\java.exe"
)

%JAVA_CMD% -cp "lib\*;drivers\*" org.flywaydb.commandline.Main %*

@REM Save the exit code
set JAVA_EXIT_CODE=%ERRORLEVEL%

@REM Restore current directory
chdir /d %OLDDIR%

@REM Exit using the same code returned from Java
EXIT /B %JAVA_EXIT_CODE%
