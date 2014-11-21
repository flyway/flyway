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

CREATE TABLE "flyway_test" (
  "ID" int NOT NULL,
  "NAME" text,
  "VAL" text,
  PRIMARY KEY ("ID")
);


INSERT INTO "flyway_test" VALUES (1,'Year','YEAR({value})'),(2,'Year, Quarter','CONCAT(YEAR({value}), CONCAT(\'-\', QUARTER({value})))'),(3,'Year, Month','CONCAT(YEAR({value}), CONCAT(\'-\', MONTH({value})))'),(4,'Year, Week','CONCAT(YEAR({value}), CONCAT(\'-\', WEEK({value})))');
