--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE CLUSTER personnel
(department NUMBER(4))
SIZE 512
STORAGE (initial 100K next 50K);