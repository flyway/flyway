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