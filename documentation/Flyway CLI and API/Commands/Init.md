---
subtitle: Init
---

The
`init` command generates a new Flyway project, complete with recommended files and configuration for a given database type.

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

Either one of `-from` or `-fromtype` must be specified (or both). If only
`-fromType` is specified, it will look for conf files in the [current working directory](<Configuration/Parameters/Flyway/Working Directory>).
If there is a
`flyway.conf`, that will be used, otherwise if there is a single conf file with another name, that will be used.
If the project being created is in the same folder as the .conf file being imported from, the .conf file will be renamed, preventing it being picked up by Flyway in the future.

### Importing a SQL Source Control project

```
flyway init -projectName=MyProject -databaseType=Sqlite -from=C:\somePath\MySqlSourceControlProject -fromType=SqlSourceControl
```

Either one of `-from` or `-fromtype` must be specified (or both). If only
`-fromType` is specified, it will look for SQL Source Control files in the [current working directory](<Configuration/Parameters/Flyway/Working Directory>).
If the project being created is in the same folder as the SQL Source Control project, the operation will transform it into a Flyway Desktop project and prevent it from working as a SQL Source Control project any more. The benefit of doing this is that version control history will be preserved.
Otherwise the original project will be unchanged.

### Importing a Source Control for Oracle project

```
flyway init -projectName=MyProject -databaseType=Sqlite -from=C:\somePath\MySourceControlForOracleProject -fromType=SourceControlForOracle
```

Either one of `-from` or `-fromtype` must be specified (or both). If only
`-fromType` is specified, it will look for Source Control for Oracle files in the [current working directory](<Configuration/Parameters/Flyway/Working Directory>).
If the project being created is in the same folder as the Source Control for Oracle project, the operation will transform it into a Flyway Desktop project and prevent it from working as a Source Control for Oracle project any more. The benefit of doing this is that version control history will be preserved.
Otherwise the original project will be unchanged.

## Complete list of parameters

### Conditionally required

| Parameter      | Namespace | Description                                                                                                                                                  |
|----------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `projectName`  | init      | The name of the project to be created or imported. This is only optional when upgrading or importing a project.                                              |
| `databaseType` | init      | The primary database type of the project to be created. This is only optional when importing a project from SQL Source Control or Source Control for Oracle. |

### Optional

| Parameter  | Namespace | Description                                                                                                                                                                                                                                                                |
|------------|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `fileName` | init      | The name of the toml file to generate. This will be resolved relative to the [working directory](<Configuration/Parameters/Flyway/Working Directory>). Defaults to `flyway.toml`.                                                                                          |
| `from`     | init      | The path of a project to import from. This will be resolved relative to the [working directory](<Configuration/Parameters/Flyway/Working Directory>). Defaults to the [working directory](<Configuration/Parameters/Flyway/Working Directory>) if `fromType` is specified. |
| `fromType` | init      | The type of project to import. Valid values are `"Conf"`, `"SqlSourceControl"`, and `"SourceControlForOracle"`.                                                                                                                                                            |

## JSON output format

```json
{
  "path": "C:\\workingDirectory\\flyway.toml",
  "projectId": "someId"
}
```