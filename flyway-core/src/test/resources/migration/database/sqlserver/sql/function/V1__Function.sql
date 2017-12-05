--
-- Copyright 2010-2017 Boxfuse GmbH
--
-- INTERNAL RELEASE. ALL RIGHTS RESERVED.
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

CREATE TABLE test_data (
  value VARCHAR(25) NOT NULL,
  PRIMARY KEY(value)
)
GO

/* Inline user-defined function. */
CREATE function reverseInlineFunc()
RETURNS TABLE
AS
RETURN (
	SELECT REVERSE(value) AS value FROM test_data
)
GO

/* Table-valued function. Filled with strictly positive integers (starting at 1) up to a upper bound. */
CREATE FUNCTION positiveIntegers(@to int)
RETURNS @oneToTen TABLE (
	number int NOT NULL
)
AS
BEGIN
	DECLARE @cnt INT = 1
	WHILE @cnt <= @to
	BEGIN
	   INSERT INTO @oneToTen VALUES (@cnt)
	   SET @cnt = @cnt + 1
	END
	RETURN
END
GO