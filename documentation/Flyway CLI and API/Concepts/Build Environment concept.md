---
subtitle: build environment
---

# Build Environment

The purpose of the build environment is to see what a database would look like, if we ran all the migrations on an empty database from V0 up to latest. To do this, flyway needs dedicated environment that flyway is allowed to manage that is used to run to migrations against.

Additionally, flyway may need to rebuild this environment if the build environment contains changes that aren't in the migrations on disk (e.g. we've removed or edited migration file after it has run). Typically, this is done through `clean`, if the clean provisioner is set on the environment - however, any form of reprovisioning is supported if a different provisioner is configured.

In Flyway `Build environment` is also sometimes referred to as the `shadow environment` or `shadow database`.

## Why is this useful ?

The build environment is necessary to calculate the changes between a database/schema model, and your migrations in order to generate a new migration with these changes in. Additionally, the build environment may be used as a test environment which can be used to run your migrations against before applying them to the production environment as a validation step (`flyway check`).

## How is this used ?

The build environment can be used when performing a `diff`, `check` or `snapshot` command.
The build environment can be configured as an ordinary environment in your toml file.

When setting the database,
ensure it a dedicated database for use by flyway as it may be cleaned/reprovisioned in order to be rebuilt to an empty state. For example, the following database uses the [`clean provisioner`](<Configuration/Provisioners/Clean Provisioner>) to tell flyway that this environment is permitted to be rebuilt using `flyway clean` if necessary:

```toml
[environments.build]
url = "jdbc:postgresql://localhost:5432/sh
user = "user"
password = "password"
provisioner = "clean"
```

For local development, it is best to put the build database on the same server as your development database. This could be a shared
centralized development environment or if you're using a local instance to develop against, then the build database can
go there. This is especially important if there are any cross-database dependencies because the build database would have
to reference these in order for the migration scripts to not fail when they are executed.

The build environment won't share the [schemas](Configuration/Parameters/Environments/Schemas) configuration from another environment. Ensure the `schemas` parameter is configured for each environment.

### Example with the `diff` and `model` command:

For more information on how to use the `diff` and `model` commands, see
the [Diff](<Concepts/Diff concept>) and [Model](<Concepts/Model concept>) pages.

Build environment could be used as a target or source with the `diff` command. For example, the following
command chains the diff command with the model command to update the schema model.

Firstly the diff command would be executed. As a result of this, the build environment would be cleaned and emptied and 
then migrated to the first version. Following this the build environment would be compared against the schema model generating a diff artifact using the `diff` command.
All differences are then applied to the schema model.

```
$ flyway diff model -diff.source=migrations -diff.target=schemaModel -diff.buildEnvironment="build" -diff.buildVersion="1"
-diff.rebuild=true

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

Applied to schemaModel
 File updated: C:\Users\FlywayUser\Project\schema-model\HRSchema\Tables\table_1.sql
```


