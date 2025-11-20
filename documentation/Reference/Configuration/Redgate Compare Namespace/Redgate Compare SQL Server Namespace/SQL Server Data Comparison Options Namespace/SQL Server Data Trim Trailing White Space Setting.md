---
subtitle: redgateCompare.sqlserver.data.options.comparison.trimTrailingWhiteSpace
---

## Description

If the data in two columns differs only by whitespace at the end of the string, the data will be considered to be identical. This option applies to the following data types:
- char
- varchar
- text
- nchar
- nvarchar
- ntext

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the data comparison options settings in SQL Server projects.

### Command-line

```powershell
./flyway diff -redgateCompare.sqlserver.data.options.comparison.trimTrailingWhiteSpace=true
```

### TOML Configuration File

```toml
[redgateCompare.sqlserver.data.options.comparison]
trimTrailingWhiteSpace = true
```
