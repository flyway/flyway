--
-- Copyright 2010-2016 Boxfuse GmbH
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

CREATE TABLE """t""" (qty INT, price INT);
INSERT INTO """t""" VALUES(3, 50);
CREATE VIEW """v""" AS SELECT qty, price, qty*price AS value FROM """t""";

CREATE VIEW features AS SELECT * FROM information_schema.sql_features;