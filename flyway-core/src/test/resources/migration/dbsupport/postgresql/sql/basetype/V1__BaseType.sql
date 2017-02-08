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

CREATE TYPE test_type;

CREATE FUNCTION test_type_in(cstring) RETURNS test_type AS
'record_in'
LANGUAGE internal STABLE STRICT COST 1;

CREATE FUNCTION test_type_out(test_type) RETURNS cstring AS
'record_out' LANGUAGE internal STABLE STRICT COST 1;

CREATE TYPE test_type(INPUT=test_type_in, OUTPUT=test_type_out);