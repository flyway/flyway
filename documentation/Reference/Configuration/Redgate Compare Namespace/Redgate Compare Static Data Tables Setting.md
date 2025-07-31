---
subtitle: redgateCompare.staticDataTables
---

{% include teams.html %}

## Description

Tables to track static data for. Note that these tables must have a primary key.

## Type

Object array with the following properties:

| Property          | Tier       | Required? | Type         | Description                                              |
|-------------------|------------|-----------|--------------|----------------------------------------------------------|
| `table`           | Teams      | Yes       | String       | The name of the table to track static data for           |
| `schema`          | Teams      | No        | String       | The schema this table lives in                           |
| `excludedColumns` | Enterprise | No        | String array | Columns to exclude tracking static data for              |
| `whereClause`     | Enterprise | No        | String       | An expression used to filter rows of the specified table |

## Default

`[]`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

Static data tables can be configured from the static data settings in Flyway Desktop.

### `whereClause`

The `whereClause` property makes it possible to filter static data in a table, by applying a `WHERE` clause to any
comparison. This is useful, for example, to exclude a particular set of test data, or to speed up a comparison.

Note that:

- The `whereClause` is only applied to live databases, it has no effect on a schema-model or snapshot comparison source.
- The `whereClause` is not validated until executed on the source or target database.
- When comparing two live databases, the `whereClause` is applied to both databases.

### TOML Configuration File

```toml
[[redgateCompare.staticDataTables]]
schema = "dbo"
table = "foo"
excludedColumns = ["x", "y"]

[[redgateCompare.staticDataTables]]
schema = "dbo"
table = "bar"
whereClause = "OrderId > 0 AND ProductId <> 'TestProduct'"
```
