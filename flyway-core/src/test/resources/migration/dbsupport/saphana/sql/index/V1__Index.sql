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

CREATE TABLE t (a INT, b NVARCHAR(10), c NVARCHAR(20));
 CREATE INDEX idx ON t(b);

CREATE COLUMN TABLE A (A VARCHAR(10) PRIMARY KEY, B VARCHAR(10));
CREATE FULLTEXT INDEX i ON A(A) FUZZY SEARCH INDEX OFF SYNC;