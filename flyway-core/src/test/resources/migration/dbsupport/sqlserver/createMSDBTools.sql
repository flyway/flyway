--
-- Copyright 2010-2016 Boxfuse GmbH
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

/*
Test script to simulate the addition of Microsoft database tools assets.
 */
CREATE TABLE db_tools_test_table (
  id NVARCHAR(25) NOT NULL PRIMARY KEY
)
GO

EXEC sp_addextendedproperty 'microsoft_database_tools_support', 1, 'SCHEMA', 'dbo', 'TABLE', 'db_tools_test_table', NULL, NULL
GO

CREATE FUNCTION db_tools_test_function()
RETURNS NVARCHAR(25)
BEGIN
	RETURN 'test'
END
GO

EXEC sp_addextendedproperty 'microsoft_database_tools_support', 1, 'SCHEMA', 'dbo', 'FUNCTION', 'db_tools_test_function', NULL, NULL
GO

CREATE PROCEDURE db_tools_test_procedure AS SELECT * FROM dual
GO

EXEC sp_addextendedproperty 'microsoft_database_tools_support', 1, 'SCHEMA', 'dbo', 'PROCEDURE', 'db_tools_test_procedure', NULL, NULL
GO

CREATE VIEW db_tools_test_view AS SELECT * FROM db_tools_test_table
GO

EXEC sp_addextendedproperty 'microsoft_database_tools_support', 1, 'SCHEMA', 'dbo', 'VIEW', 'db_tools_test_view', NULL, NULL
GO
