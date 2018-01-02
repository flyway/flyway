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

CREATE TABLE tab (id int NOT NULL IDENTITY, qty INT, price INT
CONSTRAINT [PK_Source] PRIMARY KEY CLUSTERED
(
[id] ASC
));
INSERT INTO tab VALUES (3, 50);
GO
CREATE VIEW v AS SELECT id, qty, price, qty*price AS value FROM tab;
