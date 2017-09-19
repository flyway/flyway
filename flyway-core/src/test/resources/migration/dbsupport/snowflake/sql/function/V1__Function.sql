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

-- Scalar function
CREATE FUNCTION MULTIPLY (a NUMBER, b NUMBER)
    RETURNS NUMBER
    COMMENT='Multiply two numbers'
AS 'a * b';

CREATE TABLE FUNC_TABLE (ID INT, VAL VARCHAR(100));

INSERT INTO FUNC_TABLE (ID, VAL) VALUES (1, 'FirstValue');
INSERT INTO FUNC_TABLE (ID, VAL) VALUES (2, 'SecondValue');
INSERT INTO FUNC_TABLE (ID, VAL) VALUES (3, 'ThirdValue');

-- Query function
CREATE FUNCTION GET_VALUE_FROM_ID (id NUMBER)
    RETURNS TABLE (VAL VARCHAR(100))
AS 'SELECT
        VAL
    FROM FUNC_TABLE f
    WHERE f.ID = id';

-- Javascript function
CREATE FUNCTION FACTORIAL (d DOUBLE)
    RETURNS DOUBLE
    LANGUAGE JAVASCRIPT
    STRICT
AS '
  if (D <= 0) {
    return 1;
  } else {
    var result = 1;
    for (var i = 2; i <= D; i++) {
      result = result * i;
    }
    return result;
  }
  ';