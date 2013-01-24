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

CREATE TABLE "${table}" (
    "version_rank" INT NOT NULL,
    "installed_rank" INT NOT NULL,
    "version" VARCHAR(50) NOT NULL,
    "description" VARCHAR(200) NOT NULL,
    "type" VARCHAR(20) NOT NULL,
    "script" VARCHAR(1000) NOT NULL,
    "checksum" INTEGER,
    "installed_by" VARCHAR(30) NOT NULL,
    "installed_on" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "execution_time" INTEGER NOT NULL,
    "success" BOOLEAN NOT NULL
);
CREATE INDEX "${table}_vr_idx" ON "${table}" ("version_rank");
CREATE INDEX "${table}_ir_idx" ON "${table}" ("installed_rank");
CREATE INDEX "${table}_s_idx" ON "${table}" ("success");
