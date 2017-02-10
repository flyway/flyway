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

CREATE TABLE CUSTOMER
(
  id                                                                                    int                identity,
  creaon_date                                                                           datetime2                  ,
  name                                                                                  varchar( 100 )             ,
  CONSTRAINT PK_CUSTOMER PRIMARY KEY( id )
)

GO

CREATE TRIGGER CUSTOMER_INSERT ON CUSTOMER AFTER
INSERT AS BEGIN
UPDATE
    CUSTOMER set creaon_date = {ts '3099-01-01 00:00:00'} FROM CUSTOMER c inner join inserted i on c.id=i.id
END

GO
CREATE TRIGGER CUSTOMER_UPDATE ON CUSTOMER AFTER
UPDATE AS BEGIN
UPDATE
    CUSTOMER set creaon_date = {ts '3099-01-01 00:00:00'} FROM CUSTOMER c inner join inserted i on c.id=i.id
END