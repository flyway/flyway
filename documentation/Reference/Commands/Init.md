---
subtitle: Init
---

## Description

The `init` command generates a new Flyway project, complete with recommended files and configuration for a given database type.

It also allows for the upgrading of Flyway .conf files, and the porting of SQL Source Control and Source Control for Oracle projects to Flyway

## Usage examples

### Initializing a new Flyway project

```
flyway init -projectName=MyProject -databaseType=Sqlite
```

### Upgrading a .conf file

```
flyway init -projectName=MyProject -databaseType=Sqlite -from=flyway.conf -fromType=Conf
```

Either one of `-from` or `-fromtype` must be specified (or both). If only `-fromType` is specified, it will look for conf files in the [current working directory](<Command-line Parameters/Working Directory Parameter>).
If there is a `flyway.conf`, that will be used, otherwise if there is a single conf file with another name, that will be used.
If the project being created is in the same folder as the .conf file being imported from, the .conf file will be renamed, preventing it being picked up by Flyway in the future.

### Importing a SQL Source Control project

```
flyway init -projectName=MyProject -databaseType=Sqlite -from=C:\somePath\MySqlSourceControlProject -fromType=SqlSourceControl
```

Either one of `-from` or `-fromtype` must be specified (or both). If only `-fromType` is specified, it will look for SQL Source Control files in the [current working directory](<Command-line Parameters/Working Directory Parameter>).
If the project being created is in the same folder as the SQL Source Control project, the operation will transform it into a Flyway Desktop project and prevent it from working as a SQL Source Control project any more. The benefit of doing this is that version control history will be preserved.
Otherwise the original project will be unchanged.

### Importing a Source Control for Oracle project

```
flyway init -projectName=MyProject -databaseType=Sqlite -from=C:\somePath\MySourceControlForOracleProject -fromType=SourceControlForOracle
```

Either one of `-from` or `-fromtype` must be specified (or both). If only `-fromType` is specified, it will look for Source Control for Oracle files in the [current working directory](<Command-line Parameters/Working Directory Parameter>).
If the project being created is in the same folder as the Source Control for Oracle project, the operation will transform it into a Flyway Desktop project and prevent it from working as a Source Control for Oracle project any more. The benefit of doing this is that version control history will be preserved.
Otherwise the original project will be unchanged.

## Parameters

### Conditionally required

| Parameter                                                                                                  | Namespace | Description                                                                                                                                                  |
|------------------------------------------------------------------------------------------------------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`projectName`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init Project Name Setting>)   | init      | The name of the project to be created or imported. This is only optional when upgrading or importing a project.                                              |
| [`databaseType`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init Database Type Setting>) | init      | The primary database type of the project to be created. This is only optional when importing a project from SQL Source Control or Source Control for Oracle. |

### Optional

| Parameter                                                                                          | Namespace | Description                            |
|----------------------------------------------------------------------------------------------------|-----------|----------------------------------------|
| [`fileName`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init File Name Setting>) | init      | The name of the TOML file to generate. |
| [`from`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init From Setting>)          | init      | The path of a project to import from.  |
| [`fromType`](<Configuration/Flyway Namespace/Flyway Init Namespace/Flyway Init From Type Setting>) | init      | The type of project to import.         |

Universal commandline parameters are listed [here](<Command-line Parameters>).

## JSON output format

```json
{
  "path": "C:\\workingDirectory\\flyway.toml",
  "projectId": "someId"
}
```

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)