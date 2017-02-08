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

CREATE TABLE PUBLIC.t1 (
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE PUBLIC.t2 (
  -- Test with a quote makes that migration fails '
  id INT NOT NULL,
  name VARCHAR(25) NOT NULL,
  PRIMARY KEY(id)
);

create view MY_VIEWS.v1 (id, name) as
  select distinct t1.id, t1.name from PUBLIC.t1
;

create view MY_VIEWS.v2 (id, name) as
  select distinct t2.id, t2.name from PUBLIC.t2

  union

  select distinct v1.id, v1.name from MY_VIEWS.v1
;