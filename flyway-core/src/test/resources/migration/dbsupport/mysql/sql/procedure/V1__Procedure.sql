--
-- Copyright 2010-2014 Axel Fontaine and the many contributors.
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
  END //
DELIMITER ;

CALL AddData();

DELIMITER $$

CREATE PROCEDURE callMe()
BEGIN
  SELECT "CALL ME" as message,
         "ANYTIME" as anytime;
END
$$

CREATE PROCEDURE callMe2()
BEGIN
  SELECT "CALL ME" as message, -- this is a valid comment
         "ANYTIME" as anytime;
END
$$

DELIMITER ;

DROP PROCEDURE IF EXISTS sp_temp;
DELIMITER $$
CREATE DEFINER=root@'localhost' PROCEDURE sp_temp()
DETERMINISTIC
  BEGIN
    DECLARE s VARCHAR(20);

    SELECT 1;

  END;
$$
DELIMITER ;

CALL sp_temp;
DROP PROCEDURE IF EXISTS sp_temp;