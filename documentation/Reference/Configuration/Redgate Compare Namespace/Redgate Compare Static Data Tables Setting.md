---
subtitle: redgateCompare.staticDataTables
---

{% include teams.html %}

## Description

Tables to track static data for. Note that these tables must have a primary key.

## Type

Object array with the following properties:

| Property          | Tier       | Required? | Type         | Description                                    |
|-------------------|------------|-----------|--------------|------------------------------------------------|
| `table`           | Teams      | Yes       | String       | The name of the table to track static data for |
| `schema`          | Teams      | No        | String       | The schema this table lives in                 |
| `excludedColumns` | Enterprise | No        | String array | Columns to exclude tracking static data for    |

## Default

`[]`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

Static data tables can be configured from the static data settings in Flyway Desktop.

### TOML Configuration File

```toml
[[redgateCompare.staticDataTables]]
schema = "dbo"
table = "foo"
excludedColumns = ["x", "y"]
 
[[redgateCompare.staticDataTables]]
schema = "dbo"
table = "bar"
```
