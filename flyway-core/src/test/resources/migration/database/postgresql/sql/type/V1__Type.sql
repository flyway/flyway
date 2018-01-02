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

CREATE TYPE my_type AS (my_type_id integer);

CREATE TYPE """my_type2""" AS (my_type_id integer);

CREATE TYPE bug_status AS ENUM ('new', 'open', 'closed');

CREATE TYPE """bug_status2""" AS ENUM ('new', 'open', 'closed');


