---
subtitle: flyway.init.projectName
---

## Description

The name of the project.
Will set [`name`](<Configuration/Name Setting>) in the generated configuration file.

## Type

String

## Default

<i>This is normally a required parameter of the
`init` command. This is only optional when upgrading or importing a project, at which point the default is the name of the source project.</i>

## Usage

### Command-line

```bash
./flyway init -name="My Project" -databaseType="sqlserver"
```
