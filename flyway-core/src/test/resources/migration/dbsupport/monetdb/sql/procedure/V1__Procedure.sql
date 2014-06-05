--
-- Copyright 2010-2014 Axel Fontaine
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
  value VARCHAR(25) NOT NULL,
  PRIMARY KEY(value)
);

DELIMITER //
CREATE PROCEDURE AddData()
  BEGIN
    INSERT INTO test_data (value) VALUES ('Hello');
  END; //
DELIMITER ;

CALL AddData();

CREATE TABLE test_callme (
  v1 VARCHAR(25) NOT NULL,
  v2 VARCHAR(25) NOT NULL
);


DELIMITER $$



CREATE PROCEDURE callMe()
BEGIN
	DECLARE result varchar(100);
	SELECT 'CALL ME' as message INTO result;
END;
$$

CREATE PROCEDURE callMe2()
BEGIN -- comment
	DECLARE result varchar(100); --- comment
	SELECT 'CALL ME' as message INTO result;
END;
$$

DELIMITER ;

CALL callMe2();

