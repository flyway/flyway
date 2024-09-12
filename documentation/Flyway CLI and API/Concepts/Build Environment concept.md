---
subtitle: build environment
---

# Build Environment

The build environment is a separate database that is only needed in your development environment. In Flyway `Build
environment` is also sometimes referred to as the `shadow environment`. It can start as
an empty database and will be small because it will only contain the schema objects and static data. It won't contain
any transactional data.

## Why is this useful ?

The build environment can be used as a test environment which can be used to run your migrations before applying them to the
production environment. This helps to identify the differences between your development and production environments,
and to test migrations in a controlled environment.

## How is this used ?

The build environment can be used when performing a `diff`, `diffApply` or `check` command.
The build environment can be configured as an ordinary environment in your toml file. When setting the database,
ensure it isn't an important database as this may be cleaned and rebuilt to an empty state.
The provisioner for the build environment should be set to [`clean`](<Configuration/Provisioners/Clean Provisioner>) in
the toml file to ensure that it could be cleaned and emptied when needed as follows:

```toml
[environments.build]
url = "jdbc:postgresql://localhost:5432/sh
user = "user"
password = "password"
provisioner = "clean"
```

It is best to put the build database on the same server as your development database. This could be a shared
centralized development environment or if you're using a local instance to develop against, then the build database can
go there. This is especially important if there are any cross-database dependencies because the build database would have
to reference these in order for the migration scripts to not fail when they are executed.

### Example with the `diff` and `diffApply` command:

For more information on how to use the `diff` and `diffApply` commands, see
the [Diff](<Concepts/Diff concept>) and [Diff Apply](<Concepts/Diff Apply concept>) pages.

Build environment could be used as a target or source in the `diff` and `diffApply` commands. For example, the following
command is chaining diff with a diffApply operation.

Firstly the diff command would be executed. As a result of this, the build environment would be cleaned and emptied and 
then migrated to the first version. Following this the build environment would be compared against the dev environment generating a diff artifact using the `diff` command.
All differences are then applied to the target dev environment.

```
$ flyway diff diffApply -diff.source=migrations -diff.target=dev -diff.buildEnvironment="build" -diff.buildVersion="1"
-diff.rebuild=true -diffApply.target=dev

Flyway {{ site.flywayVersion }} by Redgate
INFO: Successfully dropped post-schema database level objects (execution time 00:00.059s)
Database: jdbc://url
Schema history table [HR].[HRSchema].[flyway_schema_history] does not exist yet
Successfully validated 1 migrations (execution time 00:00.099s)
Creating Schema History table [HR].[HRSchema].[flyway_schema_history] ...
Current version of schema [HR]: << Empty Schema >>
Migrating schema [HRSchema] to version "1 - first"
Successfully applied 1 migrations to schema [HRSchema], now at version v1 (execution time 00:00.112s)
diff artifact generated: C:\Users\Projects\artifact
+-----------------------------+--------+-------------+----------+-----------+
| Id                          | Change | Object Type | Schema   | Name      |
+-----------------------------+--------+-------------+----------+-----------+
| cFfTuinTzlVluhBSmi5ZXQB4kSA | Add    | Table       | HRSchema | table_1   |
+-----------------------------+--------+-------------+----------+-----------+

Applied to dev
```


