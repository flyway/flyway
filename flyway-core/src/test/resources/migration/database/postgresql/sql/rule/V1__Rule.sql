--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE some_table (
  id int
);

CREATE TABLE some_other_table (
  id int
);

CREATE TABLE some_other_table2 (
  id int
);

CREATE OR REPLACE RULE my_rule AS
ON DELETE TO some_table
DO ALSO
(
DELETE FROM some_other_table WHERE some_other_table.id=123;
DELETE FROM some_other_table2 WHERE some_other_table2.id=123;
);

CREATE TABLE some_other_table4 (
  id int
);
