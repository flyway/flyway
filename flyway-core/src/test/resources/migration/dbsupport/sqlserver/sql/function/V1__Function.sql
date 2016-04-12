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

CREATE FUNCTION [dbo].[svfCommandName]
  (
    @xml Xml
  )
  RETURNS nvarchar(50)
  WITH SCHEMABINDING
AS
  BEGIN
    return  @xml.value('local-name(/*[1])','nvarchar(max)')
  END
GO

CREATE TABLE [dbo].[EventStream](
  [EventId] [uniqueidentifier] NOT NULL,
  [EventData] [xml] NULL,
  [EventDate] [datetime] NOT NULL,
  [EventState] [int] NULL,
  [CommandName]  AS ([dbo].[svfCommandName]([EventData])),
  CONSTRAINT [PK_EventStream] PRIMARY KEY CLUSTERED
    (
      [EventId] ASC
    )WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

ALTER TABLE [dbo].[EventStream] ADD  CONSTRAINT [DF_EventStream_EventDate]  DEFAULT (getdate()) FOR [EventDate]
GO