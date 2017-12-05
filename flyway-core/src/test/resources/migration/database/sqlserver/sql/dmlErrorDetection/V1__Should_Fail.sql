--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE ${tableName} (
  id int NOT NULL PRIMARY KEY
);

GO

INSERT INTO ${tableName} (id) VALUES(1);
INSERT INTO ${tableName} (id) VALUES(2);

-- the next statement should fail because of PK violation
INSERT INTO ${tableName} (id) VALUES(1);

