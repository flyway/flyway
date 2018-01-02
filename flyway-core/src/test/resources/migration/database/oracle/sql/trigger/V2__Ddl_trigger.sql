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

create table DDL_TRIGGER_LOG(BAR number);
create sequence DDL_TRIGGER_LOG_SEQ;

CREATE TRIGGER DDL_TRIGGER_AFTER
after ddl on schema
  begin
    insert into DDL_TRIGGER_LOG(BAR)
    values(DDL_TRIGGER_LOG_SEQ.nextval);
  end;
/

CREATE TRIGGER DDL_TRIGGER_BEFORE
before ddl on schema
  begin
    insert into DDL_TRIGGER_LOG(BAR)
    values(DDL_TRIGGER_LOG_SEQ.nextval);
  end;
;