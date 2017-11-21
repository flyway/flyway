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

CREATE TABLE test_data (
  value/*test*/ /*test*/VARCHAR(25) NOT NULL PRIMARY KEY
);

CREATE FUNCTION AddData() RETURNS INTEGER
AS $$
    BEGIN
     INSERT INTO test_data (value) VALUES ('Hello');
     RETURN 1;
   END;
 $$ LANGUAGE plpgsql;

SELECT *  INTO TEMP adddata_temp_table FROM AddData() ;

CREATE FUNCTION add(integer, integer) RETURNS integer
    LANGUAGE sql/*test*/ IMMUTABLE STRICT
    AS $_$select $1 + $2;$_$;
    
CREATE FUNCTION """add2"""(integer, integer) RETURNS integer
    LANGUAGE sql/*test*/ IMMUTABLE STRICT
    AS $_$select $1 + $2;$_$;

CREATE FUNCTION inc(i integer) RETURNS VARCHAR(25)
    LANGUAGE sql
    AS $$SELECT * FROM test_data$$;    