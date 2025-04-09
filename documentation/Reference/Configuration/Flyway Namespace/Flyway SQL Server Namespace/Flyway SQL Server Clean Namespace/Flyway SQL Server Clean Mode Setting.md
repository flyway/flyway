---
subtitle: flyway.sqlserver.clean.mode
---

## Description

This configures how the [clean](Commands/Clean) command works to enable Flyway to delete objects beyond the schemas specified in the schemas configuration option.

### Valid values

- `default` : The default behavior
- `schema`: All database schemas will be cleaned, excluding built-in schemas.
- `all`: All database objects will be dropped, including all schemas

#### Objects dropped in `all` mode

- `XML Schema Collections` (at the schema-level)
- `Column encryption keys`
- `Column master keys`
- `Symmetric keys`
- `Event notifications`
- `Fulltext stoplists`
- `Registered search property lists`
- `Application roles` and `Database roles` that aren't 'fixed roles' (built-in roles)

#### Interaction with schema parameter

- An error will be raised if `flyway.sqlserver.clean.mode` is set to `schema` or
  `all`, and [schemas](<Configuration/Environments Namespace/Environment Schemas Setting>) is also configured.
    - This is to prevent accidental deletion of schemas, as Clean Mode essentially overrides the behavior of [schemas](<Configuration/Environments Namespace/Environment Schemas Setting>).

#### Excluding specific schemas

There is also a
[
`flyway.sqlserver.clean.schemas.exclude` parameter](<Configuration/Flyway Namespace/Flyway SQL Server Namespace/Flyway SQL Server Clean Namespace/Flyway SQL Server Clean Schemas Exclude Setting>) which takes an array of schemas to exclude.
These schemas won't be dropped or cleaned in `schema` or `all` mode.

## Type

String

## Default

`default`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[flyway.sqlserver.clean]
mode = "all"
```

### Configuration File

```properties
flyway.sqlserver.clean.mode=all
flyway.sqlserver.clean.schemas.exclude=schema1,schema2
```

## Notes

- This is **only** available for SQL Server
- This is **only** available in the configuration file - no CLI, Maven, Gradle, etc
- When running clean, no users will be dropped to prevent unintended loss of connectivity. You can explicitly remove Users via a [callback script](https://documentation.red-gate.com/flyway/flyway-concepts/callbacks).