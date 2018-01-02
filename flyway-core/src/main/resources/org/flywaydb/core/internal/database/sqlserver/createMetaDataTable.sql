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

CREATE TABLE [${schema}].[${table}] (
    [installed_rank] INT NOT NULL,
    [version] NVARCHAR(50),
    [description] NVARCHAR(200),
    [type] NVARCHAR(20) NOT NULL,
    [script] NVARCHAR(1000) NOT NULL,
    [checksum] INT,
    [installed_by] NVARCHAR(100) NOT NULL,
    [installed_on] DATETIME NOT NULL DEFAULT GETDATE(),
    [execution_time] INT NOT NULL,
    [success] BIT NOT NULL
);
ALTER TABLE [${schema}].[${table}] ADD CONSTRAINT [${table}_pk] PRIMARY KEY ([installed_rank]);

CREATE INDEX [${table}_s_idx] ON [${schema}].[${table}] ([success]);
GO
