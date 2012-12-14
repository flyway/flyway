--
-- Copyright (C) 2010-2012 the original author or authors.
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

DECLARE @pk VARCHAR(MAX);
SET @pk = (SELECT name FROM sys.key_constraints WHERE SCHEMA_NAME(schema_id) = N'${schema}' AND OBJECT_NAME(parent_object_id) = N'${table}');

DECLARE @DROP_TEMPLATE VARCHAR(MAX);
SET @DROP_TEMPLATE = 'ALTER TABLE [${schema}].[${table}] DROP CONSTRAINT {pk}';

DECLARE @SQL_SCRIPT VARCHAR(MAX);
SET @SQL_SCRIPT = REPLACE(@DROP_TEMPLATE, '{pk}', @pk)
EXECUTE (@SQL_SCRIPT)
GO

CREATE INDEX [${table}_vr_idx] ON [${schema}].[${table}] ([version_rank]);
CREATE INDEX [${table}_ir_idx] ON [${schema}].[${table}] ([installed_rank]);
CREATE INDEX [${table}_s_idx] ON [${schema}].[${table}] ([success]);
GO
