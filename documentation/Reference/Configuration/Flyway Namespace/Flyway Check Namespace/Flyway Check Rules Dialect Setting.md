---
subtitle: flyway.check.rulesDialect
---



{% include commandlineonly.html %}

## Description

You can configure this parameter to specify the SQL dialect for analysis, overriding Flyway's automatic dialect detection based on the JDBC connection string. This is useful when you want to enforce specific dialect rules regardless of the database type Flyway is configured to use.

See [Code Analysis](https://documentation.red-gate.com/flyway/flyway-concepts/code-analysis) for more information.

## Type

String

## Default

If not specified, Flyway will automatically detect the SQL dialect based on the database connection configuration. If auto-detection fails, Flyway will fall back to default dialects: `ansi` for `SQLFluff` and `text` for Regex rules.

## Possible Values

- `ansi`
- `bigquery`
- `clickhouse`
- `databricks`
- `db2`
- `duckdb`
- `mariadb`
- `mysql`
- `oracle`
- `postgres`
- `redshift`
- `snowflake`
- `sqlite`
- `tsql`
- `text`

**Note**: Values are case insensitive.

## Usage

### Command-line

```powershell
./flyway check -code -check.rulesDialect=postgres
```

### TOML Configuration File

```toml
[flyway.check]
rulesDialect = "postgres"
```