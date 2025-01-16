---
subtitle: redgateCompare.sqlserver.data.options.comparison.useChecksumComparison
---

## Description

Performs a checksum on each table prior to comparison. The tables are compared only if their checksums differ. This can improve performance.

Note that the built-in SQL Server checksum is used. SQL Server's checksum can occasionally fail to return different checksum values when the data sources differ (for example, text and image columns are skipped). For more information, refer to your SQL Server documentation.

Checksum comparisons can't be used with backups or scripts folders because the checksum operation is not available without SQL Server.

This option can't be used with the 'Show identical values in results' option because the contents of identical tables are not retrieved from the server.

## Type

Boolean

## Default

`false`

## Usage

This setting can't be configured other than in a TOML configuration file.

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.comparison]
useChecksumComparison = true
```
