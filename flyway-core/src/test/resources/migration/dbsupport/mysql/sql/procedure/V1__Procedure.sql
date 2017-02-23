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

DELIMITER //
CREATE PROCEDURE get_stats_bonus_cumul (isdn BIGINT, bid VARCHAR(100), dt0 DATE, dt1 DATE)

BEGIN
DECLARE sql_cmd VARCHAR(10000);
SET sql_cmd = CONCAT(
    'SELECT
    "',dt0,'" AS from_date,
FROM stats_bonus s1
WHERE s1.agg_date="',dt1,'"');

SET @sqlstatement = sql_cmd;
PREPARE sqlquery FROM @sqlstatement;
EXECUTE sqlquery;
DEALLOCATE PREPARE sqlquery;
END
//

DELIMITER ;