---
pill: cli_diff
subtitle: 'Command-line: diff'
---

# Command-line: diff - Preview

{% include enterprise.html %}

Compares two comparison sources and returns a summary of the differences. A comparison source can either be a database
environment, build environment, a schema model folder, a snapshot or empty type.
Additionally, the `diff` command will generate and store the differences between the two comparison sources in the artifact
that can subsequently be used with the `generate`, `diffApply` and `diffText` commands.

<img src="assets/command-diff.png" alt="diff">

## Usage

<pre class="console"><span>&gt;</span> flyway diff [options]</pre>

## Options

The following options can be provided to the `diff` command in the format -key=value:

- `diff.source` [REQUIRED] - The source to use for the diff operation. Valid values:
    - `<<env>>` - uses the environment named `<<env>>`
    - `empty` - models an empty database
    - `schemaModel` - the schema model folder referenced by schemaModelLocation
    - `migrations` - uses a buildEnvironment to represent the state of database after specified migrations have been applied
    - `snapshot:<<path>>` - uses a snapshot file at the specified path
- `diff.target` [REQUIRED] - The target to use for the diff operation. See: `diff.source`
- `diff.buildEnvironment` - If source/target is migrations, this specifies the environment to use as the build
  environment
- `diff.buildVersion` - If source/target is migrations, this specifies migration version to migrate the build
  environment to
- `diff.buildCherryPick` - If source/target is migrations, this specifies list of migrations to migrate the build
  environment with
- `diff.rebuild` - If source/target is migrations, forces a reprovision (rebuild) of the build environment
- `diff.snapshotSchemas` - The schemas used for a snapshot comparison source/target
- `diff.includeDefinitions` - Include object definitions in the diff output (json only)
- `diff.includeFlywayObjects` - Should the diff include flyway objects (e.g. schema history table)
- `diff.artifactFilename` - The output location of the diff artifact. Default: %temp%/flyway.artifact.diff

## Configuration

- When using `schemaModel` as a comparison source, it is necessary to specify the location of the schema model folder,
  which can be done using the `schemaModelLocation` property.
  It may be necessary to specify the schemas that should be compared, which can be done using the `schemaModelSchemas`
  property. Both of these properties should be configured under the flyway namespace.
  Example configuration that sets these properties is shown below:

```toml
[flyway]
schemaModelLocation = "./schema-model"
schemaModelSchemas = ["HRSchema"]
```

- In the case where an environment shares a name with one of the other values, it can be prefixed with `env:` to avoid ambiguity. For example, `env:dev`.

## Build environments

A [build environment](<Concepts/Build Environment concept>) is an environment that flyway is permitted to clean, manage and run migrations against in order to see what a database would look like if we ran all the migrations from an empty environment up to latest. This is typically used when generating a new migration, as we need to see what changes are not yet captured by a migration script. 

The build environment may need to be rebuilt (reprovisioned) by flyway, if a migration script has changed on disk after being executed against this environment - as a result this environment should be one you have configured as being permitted to be reprovisioned by flyway (e.g. by `flyway clean` using the `provisioner=clean` setting in the environment toml).

## Examples

### Comparing an environment against a schema model

Here the environment will contain a database with a single view and table, `view1` and `table1` respectively. The
schemaModel will have the same `table1` with modifications, and an additional `table2` table.
The `diff` command can then be used to compare the `dev` environment to the schema model which would list out the object
level differences between them.
<pre class="console">&gt; flyway diff -diff.source=dev -diff.target=schemaModel -diff.artifactFilename=.\diffArtifacts\artifact

Flyway {{ site.flywayVersion }} by Redgate
diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
+-----------------------------+--------+-------------+--------+----------------+
| Id                          | Change | Object Type | Schema | Name           |
+-----------------------------+--------+-------------+--------+----------------+
| mYwo9SPg2QEXPt8NaQlzTOW585o | Add    | View        | HR     | view1          |
| LEIqXw5rFvQUxosOl_zdDfBYyUA | Edit   | Table       | HR     | table1         |
| uRqqxnJVd2ostltNTo8j1WAWuCQ | Delete | Table       | HR     | table2         |
+-----------------------------+--------+-------------+--------+----------------+
</pre>

### Comparing a snapshot against an empty source with JSON output

In this example a snapshot has to be generated prior to performing `diff`. The snapshot is a json file that could be
generated by performing the `snapshot` command against an environment/schemaModel. In this scenario the snapshot is of
an environment which contains two tables, `Table_1` and `Table_2`.
The `diff` command will print the above two tables as differences.
<pre class="console">&gt; flyway diff -diff.source=snapshot:./snapshot/snapshot.json -diff.target=empty -diff.artifactFilename=.\diffArtifacts\artifact -outputType=json

Flyway {{ site.flywayVersion }} by Redgate
diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
{
  "differences" : [ {
    "id" : "0Vq7ldnZ06ES23tylJKD2KHrM5M",
    "differenceType" : "Add",
    "objectType" : "Table",
    "from" : {
      "schema" : "dbo",
      "name" : "Table_1",
      "definition" : null
    },
    "to" : null
  }, {
    "id" : "fvGooqeU94wq6HTCoAUlwvkd_bc",
    "differenceType" : "Add",
    "objectType" : "Table",
    "from" : {
      "schema" : "dbo",
      "name" : "Table_2",
      "definition" : null
    },
    "to" : null
  } ],
  "sourcePreparationInfo" : null,
  "targetPreparationInfo" : null
}
</pre>

### Comparing a build environment against an environment after migrating the build environment to a specified build version with rebuild

To perform this scenario of `diff` as a prerequisite the build environment has to be configured to use
the `clean` provisioner in order to reprovision the environment upon setting the `rebuild` flag to true. The migration
directory contains four migrations, `V1__first.sql`, `V2__table1.sql`,`V3__view1.sql` and `V4__table2.sql` with the
target environment not being migrated to these versions. The build environment contains a table `table_3` in the schema tested in this example.

As a result of this operation the build environment would be cleaned and all migration scripts including the build
version `3` specified would be run and then be compared against the target environment which contains only the schema and schema_history table. The
differences listed out would contain only the objects created from the migrations scripts specified as ADD type changes since the build environment is rebuild.

<pre class="console">&gt; flyway diff -diff.source=migrations -diff.target=prod -diff.buildEnvironment="build" -diff.buildVersion="3" -diff.rebuild=true -diff.artifactFilename=.\diffArtifacts\artifact

Flyway {{ site.flywayVersion }} by Redgate
INFO: Successfully dropped post-schema database level objects (execution time 00:00.059s)
Database: jdbc://url
Schema history table [HR].[HRSchema].[flyway_schema_history] does not exist yet
Successfully validated 3 migrations (execution time 00:00.099s)
Creating Schema History table [HR].[HRSchema].[flyway_schema_history] ...
Current version of schema [HR]: << Empty Schema >>
Migrating schema [HRSchema] to version "1 - first"
Migrating schema [HRSchema] to version "2 - table1"
Migrating schema [HRSchema] to version "3 - view1"
Successfully applied 3 migrations to schema [HRSchema], now at version v3 (execution time 00:00.228s)
diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
+-----------------------------+--------+-------------+----------+-----------+
| Id                          | Change | Object Type | Schema   | Name      |
+-----------------------------+--------+-------------+----------+-----------+
| cFfTuinTzlVluhBSmi5ZXQB4kSA | Add    | Table       | HRSchema | allTables |
| a.o0T8ULN8u.YVZIz6UVT7dbhC0 | Add    | Table       | HRSchema | table_1   |
| .4vZ5aLCnQIafQFb5T5O2MkLX6U | Add    | View        | HRSchema | view_1    |
+-----------------------------+--------+-------------+----------+-----------+
</pre>

### Comparing a build environment against an environment after migrating the build environment with a list of migrations

The migration directory contains four migrations, `V1__first.sql`, `V2__table1.sql`,`V3__view1.sql` and `V4__table2.sql` with the target
environment not being migrated to these versions. The build environment contains a table `table_3` in the schema tested in this example.

As a result of this operation only versions `2 and 4` will be applied to the build environment, which will
then be compared against the target environment which contains only the schema and schema_history table. The differences listed out would
contain only the objects created from the migrations scripts run as ADD type changes since the build environment is rebuilt.

<pre class="console">&gt; flyway diff -diff.source=migrations -diff.target=prod -diff.buildEnvironment="build" -diff.buildCherryPick="2,4" -diff.artifactFilename=.\diffArtifacts\artifact

Schema history table [HR].[HRSchema].[flyway_schema_history] does not exist yet
Successfully validated 4 migrations (execution time 00:00.083s)
Creating Schema History table [HR].[HRSchema].[flyway_schema_history] ...
Current version of schema [HRSchema]: << Empty Schema >>
Migrating schema [HRSchema] to version "2 - table1"
Migrating schema [HRSchema] to version "4 - table2"
Successfully applied 2 migrations to schema [HRSchema], now at version v4 (execution time 00:00.137s)
diff artifact generated: C:\Users\Projects\diffArtifacts\artifact
+-----------------------------+--------+-------------+----------+---------+
| Id                          | Change | Object Type | Schema   | Name    |
+-----------------------------+--------+-------------+----------+---------+
| a.o0T8ULN8u.YVZIz6UVT7dbhC0 | Add    | Table       | HRSchema | table_1 |
| 1gvOcO43loujJCPktuUkhBMFbSI | Add    | Table       | HRSchema | table_2 |
+-----------------------------+--------+-------------+----------+---------+
</pre>