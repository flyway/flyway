--
-- Copyright (C) 2010-2013 the original author or authors.
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
    "version_rank" NUMBER(38,0) NOT NULL,
    "installed_rank" NUMBER(38,0) NOT NULL,
    "version" VARCHAR2(50) NOT NULL,
    "description" VARCHAR2(200) NOT NULL,
    "type" VARCHAR2(20) NOT NULL,
    "script" VARCHAR2(1000) NOT NULL,
    "checksum" NUMBER(38,0),
    "installed_by" VARCHAR2(100) NOT NULL,
    "installed_on" TIMESTAMP DEFAULT SYSDATE NOT NULL,
    "execution_time" NUMBER(38,0) NOT NULL,
    "success" NUMBER(1) NOT NULL
);
ALTER TABLE "${schema}"."${table}" ADD CONSTRAINT "${table}_pk" PRIMARY KEY ("version");

CREATE INDEX "${schema}"."${table}_vr_idx" ON "${schema}"."${table}" ("version_rank");
CREATE INDEX "${schema}"."${table}_ir_idx" ON "${schema}"."${table}" ("installed_rank");
CREATE INDEX "${schema}"."${table}_s_idx" ON "${schema}"."${table}" ("success");
