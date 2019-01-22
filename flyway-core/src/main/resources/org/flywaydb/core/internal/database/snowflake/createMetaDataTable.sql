--
-- Copyright 2010-2018 Boxfuse GmbH
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

CREATE TABLE "${schema}"."${table}" (
	"installed_rank" NUMBER(38,0) NOT NULL,
	"version" VARCHAR(50),
	"description" VARCHAR(200),
	"type" VARCHAR(20) NOT NULL,
	"script" VARCHAR(1000) NOT NULL,
	"checksum" NUMBER(38,0),
	"installed_by" VARCHAR(100) NOT NULL,
	"installed_on" TIMESTAMP_LTZ(9) NOT NULL DEFAULT CURRENT_TIMESTAMP(),
	"execution_time" NUMBER(38,0) NOT NULL,
	"success" BOOLEAN NOT NULL,
	primary key ("installed_rank")
);
