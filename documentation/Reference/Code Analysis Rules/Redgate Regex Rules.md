---
subtitle: Redgate Regex Rules
---
{% include enterprise.html %}

## RX001: _DROP TABLE statement_
**Dialects:** All

Dropping a table is likely to result in the loss of data so should be investigated before continuing.

## RX002: _Attempt to change password_
**Dialects:** Oracle/PostgreSQL/TSQL

Changing passwords through a DB migration is not considered best practice

## RX003: _TRUNCATE statement used_
**Dialects:** All

This operation is likely to result in a loss of data so should be investigated before continuing

## RX004: _DROP COLUMN statement used_
**Dialects:** All

This operation is likely to result in a loss of data so should be investigated before continuing

## RX005: _GRANT TO PUBLIC statement used_
**Dialects:** All

It is not common to access to this degree so should be investigated before continuing 

## RX006: _GRANT WITH GRANT OPTION statement used_
**Dialects:** All

Allows grantee to grant additional permissions and so it becomes difficult to track the scope of permissions 

## RX007: _GRANT WITH ADMIN OPTION statement used_
**Dialects:** All

Allows grantee to grant administrative permissions and so it becomes difficult to control the scope of permissions 

## RX008: _ALTER USER statement used_
**Dialects:** All

Modifies the properties of an existing user and should be investigated before continuing

## RX009: _GRANT ALL statement used_
**Dialects:** All

It is not common to access to this degree so should be investigated before continuing
## RX010: _CREATE ROLE statement used_
**Dialects:** All

## RX011: _ALTER ROLE statement used_
**Dialects:** All

This is used to modify user accounts so should be investigated before continuing

## RX012: _DROP PARTITION statement used_
**Dialects:** All

## RX013: _CREATE TABLE statement without a PRIMARY KEY constraint_
**Dialects:** All

## RX014: _No Table Description_
**Dialects:** TSQL

A table has been created but has no `MS_Description` property added

It is a good practice to include a description in the `MS_Description` extended property to document the purpose of a table.