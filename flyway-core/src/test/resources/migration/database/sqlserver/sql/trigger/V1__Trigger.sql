--
-- Copyright 2010-2017 Boxfuse GmbH
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

CREATE TABLE Customers (
 CustomerId smallint identity(1,1),
 Name nvarchar(255),
 Priority tinyint
   CONSTRAINT [PK_Source] PRIMARY KEY CLUSTERED
     (
       [CustomerId] ASC
     ));
CREATE TABLE Sales (
 TransactionId smallint identity(1,1),
 CustomerId smallint,
 [Net Amount] int,
 Completed bit
   CONSTRAINT [PK_Source1] PRIMARY KEY CLUSTERED
     (
       [TransactionId] ASC
     ));
go

CREATE TRIGGER dbo.Update_Customer_Priority
  ON Sales
AFTER INSERT, UPDATE, DELETE
AS
WITH CTE AS (
  select CustomerId from inserted
  union
  select CustomerId from deleted
)
UPDATE Customers
SET
  Priority =
    case
      when t.Total < 10000 then 3
      when t.Total between 10000 and 50000 then 2
      when t.Total > 50000 then 1
      when t.Total IS NULL then NULL
    end
FROM Customers c
INNER JOIN CTE ON CTE.CustomerId = c.CustomerId
LEFT JOIN (
  select
    Sales.CustomerId,
    SUM([Net Amount]) Total
  from Sales
  inner join CTE on CTE.CustomerId = Sales.CustomerId
  where
    Completed = 1
  group by Sales.CustomerId
) t ON t.CustomerId = c.CustomerId
go

insert into Customers select N'MS SQL Server Team', NULL
insert into Customers select N'MS Windows Team', NULL
insert into Customers select N'MS Internet Explorer Team', NULL
insert into Sales select 1, 5000, 1
insert into Sales select 2, 45000, 1
insert into Sales
  select CustomerId, 7500, 1 from Customers