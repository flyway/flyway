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

-- State transition function:
CREATE  FUNCTION create_select(acc text, instr text) RETURNS text AS $$
  BEGIN
    IF acc IS NULL OR acc = '' THEN
      RETURN replace(instr,'.','_') ;
    ELSE
      RETURN acc || ', ' || replace(instr,'.','_') ;
    END IF;
  END;
$$ LANGUAGE plpgsql;

-- Aggregate function
CREATE AGGREGATE textcat_all(
  basetype    = text,
  sfunc       = create_select,
  stype       = text,
  initcond    = ''
);