---
subtitle: SQL Server Filter Format
---

## Format

SQL Server filter files have the .scpf extension.
Under the hood they are structured as XML.

Example:
```xml
<?xml version="1.0" encoding="utf-8" standalone="yes"?>
<NamedFilter version="1" type="SQLCompareFilter">
  <FilterName>Filter</FilterName>
  <Filter version="1" type="DifferenceFilter">
    <FilterCaseSensitive>False</FilterCaseSensitive>
    <Filters version="1">
      <None version="1">
        <Include>True</Include>
        <Expression>TRUE</Expression>
      </None>
      <StoredProcedure version="1">
        <Include>False</Include>
        <Expression>((@SCHEMA = 'dbo') AND (@NAME = 'getCustomerOrders'))</Expression>
      </StoredProcedure>
      <Table version="1">
        <Include>True</Include>
        <Expression>TRUE</Expression>
      </Table>
      <User version="1">
        <Include>True</Include>
        <Expression />
      </User>
    </Filters>
  </Filter>
</NamedFilter>
```

The `Filters` node contains one or more nodes per object type.

Within each object type, is an `Expression` node.
This expression will be evaluated against every difference returned as the result of a database comparison.
If the expression evaluates to true and the `Include` node is set to true, the difference will be included, and if false, it will be filtered out.
Conversely, if the expression evaluates to true and the `Include` node is set to false, the difference will be filtered out, and if false, it will be included. 

The `None` entry determines what the default behaviour is, irrespective of object type.

In the example above, `Table` objects will always be included, `User` objects will always be excluded, and all stored procedures will be included, with the exception of one named `dbo.getCustomerOrders`.

## Syntax

The Filter file uses standard SQL Server `LIKE` and `NOT LIKE` syntax, which supports the following wildcards:

| Wildcard                         | Behavior                            |
|----------------------------------|-------------------------------------|
| _                                | Matches any single character.       |
| %                                | Matches any sequence of characters. |
| \[^\<range \| character list\>\] | Matches a range or character list.  |

Example:
```xml
<!-- Here we are only including tables that begin with tbl_ -->
<!-- Note that since the underscore character is interpreted as a wildcard, it needs to be escaped in square brackets to denote the character -->
    <Table version="1">
        <Include>True</Include>
        <Expression>((@NAME LIKE 'tbl[_]%'))</Expression>
    </Table>

<!-- Here we are excluding tables that begin with test_ OR tables that begin with todo_ -->
    <Table version="1">
        <Include>False</Include>
        <Expression>((@NAME LIKE 'test[_]%')) OR ((@NAME LIKE 'todo[_]%'))</Expression>
    </Table>

<!-- Here we are excluding tables that belong to a schema named 'test' -->
    <Table version="1">
        <Include>False</Include>
        <Expression>((@SCHEMA = 'test'))</Expression>
    </Table>
```