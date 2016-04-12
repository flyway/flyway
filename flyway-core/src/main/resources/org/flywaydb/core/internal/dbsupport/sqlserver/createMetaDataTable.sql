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
