--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

create and compile java source named "org/flywaydb/core/internal/database/oracle/sql/java/JavaSource" as
package org.flywaydb.core.internal.database.oracle.sql.java;
public class JavaSource {
    int[] x = { 1, 2, 3 };
};
/

create java class using blob
select
'CAFEBABE0000002F00140A000400100900030011070012070013010001780100025B490100063C69'||
'6E69743E010003282956010004436F646501000F4C696E654E756D6265725461626C650100124C6F'||
'63616C5661726961626C655461626C65010004746869730100404C6F72672F666C7977617964622F'||
'636F72652F696E7465726E616C2F6462737570706F72742F6F7261636C652F73716C2F6A6176612F'||
'4A617661436C6173733B01000A536F7572636546696C6501000E4A617661436C6173732E6A617661'||
'0C000700080C0005000601003E6F72672F666C7977617964622F636F72652F696E7465726E616C2F'||
'6462737570706F72742F6F7261636C652F73716C2F6A6176612F4A617661436C6173730100106A61'||
'76612F6C616E672F4F626A6563740021000300040000000100000005000600000001000100070008'||
'000100090000004600050001000000182AB700012A06BC0A5903044F5904054F5905064FB50002B1'||
'00000002000A0000000A00020000000300040005000B0000000C000100000018000C000D00000001'||
'000E00000002000F'
from dual;
/

create java resource named "org/flywaydb/core/internal/database/oracle/sql/java/JavaResource.txt" using clob
select
'Hello Flyway!'
from dual;
/