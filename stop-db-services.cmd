@REM
@REM Copyright 2010-2017 Boxfuse GmbH
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

net stop SQLBrowser
net stop MSSQL$SQLEXPRESS
net stop postgresql-x64-9.0
net stop OracleXETNSListener
net stop OracleServiceXE
net stop MySQL
net stop MariaDB
net stop DB2MGMTSVC_DB2COPY1
net stop DB2DAS00
net stop DB2REMOTECMD_DB2COPY1
net stop DB2