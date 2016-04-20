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

CREATE TABLE test_user (
  name VARCHAR(25)
);
-- The table should be created with a different delimiter to support \n in values, but this is not supported in Hive 1.2.1:
-- LINES TERMINATED BY '\001'
-- https://issues.apache.org/jira/browse/HIVE-5999
--
-- Another workaround would be to use TBLPROPERTIES ("textinputformat.record.delimiter"="#");
-- But for some reason all values end up with an additional \n at the end.