---
pill: clean.mode
subtitle: flyway.sqlserver.clean.mode
---
# Clean Mode

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
- An error will be raised if `flyway.sqlserver.clean.mode` is set to `schema` or `all`, and [schemas](Configuration/Parameters/Environments/Schemas) is also configured.
    - This is to prevent accidental deletion of schemas, as Clean Mode essentially overrides the behavior of [schemas](Configuration/Parameters/Environments/Schemas).

#### Excluding specific schemas
There is also a `flyway.sqlserver.clean.schemas.exclude` parameter which takes a comma-separated list of schemas to exclude. 
These schemas won't be dropped or cleaned in `schema` or `all` mode.

## Default
`default`

## Usage
### TOML Configuration File
```toml
[flyway]
sqlserver.clean.mode = "all"
sqlserver.clean.schemas.exclude = ["schema1", "schema2"] 
```
### Configuration File
```properties
flyway.sqlserver.clean.mode=all
flyway.sqlserver.clean.schemas.exclude=schema1,schema2
```

## Notes
- This is **only** available for SQL Server 
- This is **only** available in the configuration file - no CLI, Maven, Gradle, etc
