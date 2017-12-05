--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
--

CREATE TABLE test_data (
  value VARCHAR(25) NOT NULL,
  PRIMARY KEY(value)
)
GO

CREATE PROCEDURE AddData
AS
BEGIN
    INSERT INTO test_data (value) VALUES ('Hello');
END
GO

EXEC AddData
GO
