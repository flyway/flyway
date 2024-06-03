---
pill: code analysis rules
subtitle: check
---
# Code Analysis Rules
Flyway can call [SQLFluff](https://www.sqlfluff.com/) and include code violations in the [`check`](/Concepts/Check concept) report.

You can find more detail of these rules in the [SQLFluff rules](https://docs.sqlfluff.com/en/stable/rules.html) description.

# Enterprise Rules
{% include enterprise.html %}

Redgate has added a set of additional rules that are of interest to customers with a larger and more complex database infrastructure.

Enterprise customers are also able to define their own rules - see [Creating Regular Expression Rules](Configuration/Creating Regular Expression Rules) 
## Flyway_L001 ( **[DEPRECATED]**, replaced by RX013)
_CREATE TABLE statement without a PRIMARY KEY constraint_

**Dialects:** All

It is best practice in most situations to define a primary key for each table. This can facilitate querying and ordering data 
## Flyway_L002 ( **[DEPRECATED]**, replaced by RX014)
_A table has been created but has no MS_Description property added_

**Dialects:** TSQL

Intrinsic documentation helps other database developers and can be used to generate documentation about the database.
## RX001
_DROP TABLE statement_

**Dialects:** All

Dropping a table is likely to result in the loss of data so should be investigated before continuing.
## RX002
_Attempt to change password_

**Dialects:** Oracle/PostgreSQL/TSQL

Changing passwords through a DB migration is not considered best practice
## RX003
_TRUNCATE statement used_

**Dialects:** All

This operation is likely to result in a loss of data so should be investigated before continuing

## RX004
_DROP COLUMN statement used_

**Dialects:** All

This operation is likely to result in a loss of data so should be investigated before continuing
## RX005
_GRANT TO PUBLIC statement used_

**Dialects:** All

It is not common to access to this degree so should be investigated before continuing 

## RX006
_GRANT WITH GRANT OPTION statement used_

**Dialects:** All

Allows grantee to grant additional permissions and so it becomes difficult to track the scope of permissions 

## RX007
_GRANT WITH ADMIN OPTION statement used_

**Dialects:** All

Allows grantee to grant administrative permissions and so it becomes difficult to control the scope of permissions 

## RX008
_ALTER USER statement used_

**Dialects:** All

Modifies the properties of an existing user and should be investigated before continuing

## RX009
_GRANT ALL statement used_

**Dialects:** All

It is not common to access to this degree so should be investigated before continuing
## RX010
_CREATE ROLE statement used_

**Dialects:** All
## RX011
_ALTER ROLE statement used_

This is used to modify user accounts so should be investigated before continuing

**Dialects:** All
## RX012
_DROP PARTITION statement used_

**Dialects:** All
## RX013
_CREATE TABLE statement without a PRIMARY KEY constraint_

**Dialects:** All
## RX014
_A table has been created but has no MS_Description property added_

**Dialects:** TSQL

This operation is likely to result in a loss of data so should be investigated before continuing