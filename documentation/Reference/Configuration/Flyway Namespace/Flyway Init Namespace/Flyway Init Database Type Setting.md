---
subtitle: flyway.init.projectName
---

## Description

The database type of the project.
Will set [`databaseType`](<Configuration/Database Type Setting>) in the generated configuration file.
The generated configuration file will contain default configurations appropriate to the specified database type.

## Type

String

## Default

<i>This is normally a required parameter of the
`init` command. This is only optional when importing a project from SQL Source Control or Source Control for Oracle.</i>

## Usage

### Command-line

```bash
./flyway init -projectName="My Project" -databaseType="sqlserver"
```
