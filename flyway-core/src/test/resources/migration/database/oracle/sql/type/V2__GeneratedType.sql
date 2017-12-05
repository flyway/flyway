--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

-- this package generates types
CREATE OR REPLACE PACKAGE PKG_PIPE_RECORD AS

TYPE t_rec IS RECORD (
    field1 NUMBER,
    field2 VARCHAR2(100)
);

TYPE t_rec_coll IS TABLE OF t_rec;

FUNCTION func RETURN t_rec_coll PIPELINED;

END;
/