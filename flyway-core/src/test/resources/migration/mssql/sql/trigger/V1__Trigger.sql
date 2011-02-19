--
-- Copyright (C) 2010-2011 the original author or authors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE test1(a1 INT);
CREATE TABLE test2(a2 INT);
CREATE TABLE test3(a3 INT NOT NULL IDENTITY PRIMARY KEY);
CREATE TABLE test4(
  a4 INT NOT NULL IDENTITY PRIMARY KEY,
  b4 INT DEFAULT 0
);
GO

CREATE TRIGGER testref BEFORE INSERT ON test1
AS
BEGIN

	DECLARE thecursor CURSOR FOR
	SELECT inserted.a1 FROM inserted
	DECLARE @a1 INT

	OPEN thecursor
	FETCH thecursor INTO @a1

	WHILE (@@FETCH_STATUS = 0) BEGIN
	    INSERT INTO test2 SET a2 = @a1;
	    DELETE FROM test3 WHERE a3 = @a1;
	    UPDATE test4 SET b4 = b4 + 1 WHERE a4 = @a1;

	    FETCH thecursor INTO @a1
	END

	CLOSE thecursor
	DEALLOCATE thecursor
END
GO

INSERT INTO test3 (a3) VALUES
  (NULL), (NULL), (NULL), (NULL), (NULL),
  (NULL), (NULL), (NULL), (NULL), (NULL);

INSERT INTO test4 (a4) VALUES
  (0), (0), (0), (0), (0), (0), (0), (0), (0), (0);
