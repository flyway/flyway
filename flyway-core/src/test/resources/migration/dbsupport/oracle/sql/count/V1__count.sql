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

create table employees ( i int, name varchar2(10), instime date);
-- sample file has 1 create stmt, 3 insert stmts, 2 update stmts , 1 delete stmts

insert into employees values (1, 'test1', sysdate);
insert into employees values (2, 'test2', sysdate);
insert into employees values (3, 'test3', sysdate);
update employees set i=4 where name='test3';
update employees set i=5 where name='test1';
delete from employees where i=2;
