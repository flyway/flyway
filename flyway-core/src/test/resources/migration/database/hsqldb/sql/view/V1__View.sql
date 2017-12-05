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