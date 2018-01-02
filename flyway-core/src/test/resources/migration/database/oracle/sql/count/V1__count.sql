--
-- Copyright 2010-2018 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--
-- Must
-- be
-- exactly
-- 13 lines
-- to match
-- community
-- edition
-- license
-- length.
--

create table employees ( i int, name varchar2(10), instime date);
-- sample file has 1 create stmt, 3 insert stmts, 2 update stmts , 1 delete stmts

insert into employees values (1, 'test1', sysdate);
insert into employees values (2, 'test2', sysdate);
insert into employees values (3, 'test3', sysdate);
update employees set i=4 where name='test3';
update employees set i=5 where name='test1';
delete from employees where i=2;
