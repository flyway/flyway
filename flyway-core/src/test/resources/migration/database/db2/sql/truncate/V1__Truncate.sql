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

CREATE TABLE CTP_STATUS
(
    STATUS_NAME  VARCHAR(35)  NOT NULL,
    STATUS_VALUE VARCHAR(256) NOT NULL,
    INSERT_TS    TIMESTAMP(6) NOT NULL,
    UPDATE_TS    TIMESTAMP(6) NOT NULL
);

ALTER TABLE "CTP_STATUS" ADD CONSTRAINT "PK_CTP_STATUS" PRIMARY KEY ("STATUS_NAME");