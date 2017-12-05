--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TYPE rating AS ENUM('positive', 'negative');
CREATE TABLE t (x rating);
INSERT INTO T VALUES ('positive');
