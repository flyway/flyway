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

CREATE TABLE address (
    id bigint NOT NULL,
    address character varying(256) NOT NULL
);

INSERT INTO address VALUES (1, '1. first
2. second');

COMMENT ON COLUMN address.address IS 'ATIVO = 1;
CONCLUIDO = 2;
CANCELADO = 0;';

