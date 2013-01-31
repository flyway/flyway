--
-- Copyright (C) 2010-2013 the original author or authors.
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

DROP INDEX ${table}_cv_idx ON ${schema}.${table};
EXEC sp_rename '${schema}.${table}', '${table}';
GO

CREATE PROCEDURE [DropUniqueConstraint]
    @columnName NVarchar(255)
AS
    DECLARE @IdxNames CURSOR

    SET @IdxNames = CURSOR FOR
        select sysindexes.name from sysindexkeys,syscolumns,sysindexes
            WHERE
                syscolumns.[id] = OBJECT_ID(N'[${schema}].[${table}]')
                AND sysindexkeys.[id] = OBJECT_ID(N'[${schema}].[${table}]')
                AND sysindexes.[id] = OBJECT_ID(N'[${schema}].[${table}]')
                AND syscolumns.name=@columnName
                AND sysindexkeys.colid=syscolumns.colid
                AND sysindexes.[indid]=sysindexkeys.[indid]
                AND (
                    SELECT COUNT(*) FROM sysindexkeys AS si2
                    WHERE si2.id=sysindexes.id
                    AND si2.indid=sysindexes.indid
                )=1
    OPEN @IdxNames
    DECLARE @IdxName Nvarchar(255)
    FETCH NEXT FROM @IdxNames INTO @IdxName

    WHILE @@FETCH_STATUS = 0 BEGIN
        DECLARE @dropSql Nvarchar(4000)

        SET @dropSql=
            N'ALTER TABLE [${schema}].[${table}]
                DROP CONSTRAINT ['+@IdxName+ N']'
        EXEC(@dropSql)

        FETCH NEXT FROM @IdxNames
        INTO @IdxName
    END
CLOSE @IdxNames
DEALLOCATE @IdxNames
GO

EXEC [DropUniqueConstraint] @columnName='script';
ALTER TABLE [${schema}].[${table}] ALTER COLUMN [script] NVARCHAR(1000) NOT NULL;

EXEC [DropUniqueConstraint] @columnName='version';
ALTER TABLE [${schema}].[${table}] ALTER COLUMN [version] NVARCHAR(50) NOT NULL;
GO

DROP PROCEDURE [DropUniqueConstraint];
GO

ALTER TABLE [${schema}].[${table}] DROP COLUMN current_version;

ALTER TABLE [${schema}].[${table}] ALTER COLUMN [description] NVARCHAR(200) NOT NULL;

ALTER TABLE [${schema}].[${table}] ALTER COLUMN [type] NVARCHAR(20) NOT NULL;
UPDATE [${schema}].[${table}] SET [type] = 'SPRING_JDBC' WHERE [type] = 'JAVA';

EXEC sp_rename '${schema}.${table}.installed_by', 'installed_by', 'COLUMN';

EXEC sp_rename '${schema}.${table}.installed_on', 'installed_on', 'COLUMN';
ALTER TABLE [${schema}].[${table}] ALTER COLUMN [installed_on] DATETIME NOT NULL;

EXEC sp_rename '${schema}.${table}.execution_time', 'execution_time', 'COLUMN';
ALTER TABLE [${schema}].[${table}] ALTER COLUMN [execution_time] INT NOT NULL;

ALTER TABLE [${schema}].[${table}] ADD [version_rank] INT;
ALTER TABLE [${schema}].[${table}] ADD [installed_rank] INT;

ALTER TABLE [${schema}].[${table}] ADD [success] BIT;
GO

UPDATE [${schema}].[${table}] SET [success] = 1 WHERE state = 'SUCCESS';
UPDATE [${schema}].[${table}] SET [success] = 0 WHERE state = 'FAILED';
ALTER TABLE [${schema}].[${table}] ALTER COLUMN [success] BIT NOT NULL;
ALTER TABLE [${schema}].[${table}] DROP COLUMN state;

