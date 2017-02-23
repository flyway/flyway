--
-- Copyright 2010-2017 Boxfuse GmbH
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

PRINT 'Hello warning!';

create table [dbo].[TableA] (
ID varchar(25) not null,
Column1 varchar(10),
Column2 varchar(10)
)
GO

create table [dbo].[TableB] (
ID varchar(25) not null,
Column1 varchar(10),
Column2 varchar(10)
)
GO

CREATE function [dbo].[fnNextId]()
returns int
as
begin
  declare @maxId int
  select @maxId = id + 1 from (
    select max(ID) as ID from TableA
    union
    select max(ID) as ID from TableB
  ) useless_alias
  return @maxId
end
GO

ALTER TABLE [dbo].[TableA] ADD  CONSTRAINT [DF_TableA_ID]  DEFAULT ([dbo].[fnNextId]()) FOR [ID]
GO

ALTER TABLE [dbo].[TableB] ADD  CONSTRAINT [DF_TableB_ID]  DEFAULT ([dbo].[fnNextId]()) FOR [ID]
GO
