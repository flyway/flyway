--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

create table EMPLOYEE ( "ID" integer not null, "NAME" varchar(100) );
alter table EMPLOYEE add primary KEY ("ID");

insert into employee values(1, 'Mark');
insert into employee values(2, 'Tommy');

CREATE ALIAS "POOR_SLAVE" FOR "EMPLOYEE";