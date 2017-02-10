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

CREATE TABLE CUSTOMER (
  firstname VARCHAR(25) NOT NULL
);

CREATE INDEX IDX_CUSTOMER_NAME ON CUSTOMER(LOWER(FIRSTNAME) ASC);
CREATE INDEX IX_CUSTOMER_NAME ON CUSTOMER(LOWER(FIRSTNAME) ASC);
CREATE INDEX I_CUSTOMER_NAME ON CUSTOMER(LOWER(FIRSTNAME) ASC);