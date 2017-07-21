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

CREATE STAGE STG
  FILE_FORMAT = (
    TYPE = CSV
    COMPRESSION = NONE
    RECORD_DELIMITER = '\r'
    FIELD_DELIMITER = '|'
    FILE_EXTENSION = '.csv'
    SKIP_HEADER = 0
    DATE_FORMAT = AUTO
    TIME_FORMAT = AUTO
    TIMESTAMP_FORMAT = AUTO
    BINARY_FORMAT = "UTF-8"
    ESCAPE = NONE
    ESCAPE_UNENCLOSED_FIELD = NONE
    TRIM_SPACE = FALSE
    FIELD_OPTIONALLY_ENCLOSED_BY = NONE
    ERROR_ON_COLUMN_COUNT_MISMATCH = TRUE
    VALIDATE_UTF8 = TRUE
    EMPTY_FIELD_AS_NULL = TRUE
    SKIP_BYTE_ORDER_MARK = TRUE
    )
  COPY_OPTIONS = (
      ON_ERROR = ABORT_STATEMENT
      ENFORCE_LENGTH = TRUE
      PURGE = TRUE
      RETURN_FAILED_ONLY = FALSE
      FORCE = TRUE
    );

CREATE TABLE EXT (ID INT, VAL INT);
INSERT INTO EXT VALUES (1, 11);
INSERT INTO EXT VALUES (2, 22);
INSERT INTO EXT VALUES (3, 33);

COPY INTO @STG/EXT
FROM EXT;

CREATE TABLE IMP (ID INT, VAL INT);

COPY INTO IMP
FROM @STG
FILE_FORMAT = (
    TYPE = CSV
    COMPRESSION = NONE
    RECORD_DELIMITER = '\r'
    FIELD_DELIMITER = '|'
    FILE_EXTENSION = '.csv'
    SKIP_HEADER = 0
    DATE_FORMAT = AUTO
    TIME_FORMAT = AUTO
    TIMESTAMP_FORMAT = AUTO
    BINARY_FORMAT = "UTF-8"
    ESCAPE = NONE
    ESCAPE_UNENCLOSED_FIELD = NONE
    TRIM_SPACE = FALSE
    FIELD_OPTIONALLY_ENCLOSED_BY = NONE
    ERROR_ON_COLUMN_COUNT_MISMATCH = TRUE
    VALIDATE_UTF8 = TRUE
    EMPTY_FIELD_AS_NULL = TRUE
    SKIP_BYTE_ORDER_MARK = TRUE
    )
FILES = ('EXT_0_0_0.csv');
