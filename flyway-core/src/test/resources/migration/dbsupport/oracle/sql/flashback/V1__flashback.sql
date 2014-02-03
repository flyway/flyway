--
-- Copyright 2010-2014 Axel Fontaine and the many contributors.
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

CREATE TABLE test (
id number(10) not null,
name varchar(20),
primary key (id)
);

CREATE TABLE table1(
  id number,
  name varchar2(20),
  test_id number(10) not null,
  foreign key (test_id) references test(id)
);

-- unfortunately you need to specify archive name, but I do not have the permissions to create it in my environment
-- so I will not even try to write it properly, with creating the archive from scratch
alter table table1 flashback archive fda_trac;
alter table test flashback archive fda_trac;
--from now on you will be unable to delete the tables with plain DROP command

insert into test values (1,'aaa');
insert into test values (2,'aaa');
insert into test values (3,'aac');

update test set name = 'cccc';

insert into table1 values (1,'daa', 1);

commit;