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

create table EMPLOYEE ( "ID" integer not null, "NAME" varchar(100) );
alter table EMPLOYEE add primary KEY ("ID");

create table ADDRESS ( "ID" integer not null, "EMPL_ID" integer not null, "STREET" VARCHAR(250) );
alter TABLE ADDRESS add PRIMARY KEY ("ID");
alter table ADDRESS add CONSTRAINT "EMPL" FOREIGN KEY("ID") REFERENCES "EMPLOYEE" ("ID");

insert into employee values(1, 'Mark');
insert into employee values(2, 'Tommy');

insert into address values(1, 1, 'Street 1');
insert into address values(2, 2, 'Street 2');

create view EMPL as select E.NAME, A.STREET from EMPLOYEE as E, ADDRESS as A where E.ID = A.EMPL_ID;

CREATE table empl_mqt as (SELECT * FROM EMPL) data initially deferred refresh deferred;

set integrity for empl_mqt immediate checked not incremental;
