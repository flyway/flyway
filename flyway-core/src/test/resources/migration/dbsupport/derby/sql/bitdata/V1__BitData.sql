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

CREATE TABLE my_table (my_uuid CHAR (16) FOR BIT DATA NOT NULL,
  CONSTRAINT pk_my_table PRIMARY KEY (my_uuid));

ALTER TABLE my_table ADD my_other_uuid CHAR (16) FOR BIT DATA
  NOT NULL DEFAULT X'0000';

INSERT INTO my_table (my_uuid) VALUES (X'0123');