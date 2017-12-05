--
-- Copyright 2010-2017 Boxfuse GmbH
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

create table parent(
id integer primary KEY
);

create table child(
id integer primary KEY ,
parent_id integer not null REFERENCES parent(id)
);

insert into parent values(1);
insert into child values(1,1);