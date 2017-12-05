--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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
