---
subtitle: 'Tutorial: Flyway Desktop migrations workflow with Flyway CLI'
---

## Introduction

The V11 release of Flyway saw several new Flyway CLI verbs removed from preview and officially released. In this
document we will explore how to use these new verbs to mirror the Flyway Desktop migrations workflow using Flyway CLI.

## Prerequisites

The examples presented here assumed you are using at least Flyway CLI version 11.0.0 and have a teams or enterprise
license.

## Initialize a project

Firstly, we will create a new SQL Server project using the `init` command as shown below:

```
$ mkdir SqlServerProject
$ cd SqlServerProject
$ flyway init "-init.projectName=SqlServerProject" "-init.databaseType=sqlserver"
```

The init command must have the following options passed in:

- `-init.projectName` - The name of the project
- `-init.databaseType` - The type of database used for the project.

Once run this will leave us with a project folder that looks as follows:

```
PS C:\Users\Flyway\FlywayProjects\SqlServerProject> ls


    Directory: C:\Users\Flyway\FlywayProjects\SqlServerProject


Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d-----        05/12/2024     14:48                migrations
d-----        05/12/2024     14:48                schema-model
-a----        05/12/2024     14:48             69 .gitignore
-a----        05/12/2024     14:48           4711 Filter.scpf
-a----        05/12/2024     14:48           3711 flyway.toml
-a----        05/12/2024     14:48              0 flyway.user.toml
```

## Defining environments

The next step is to define development, shadow and prod environments that will be used as arguments to flyway CLI
commands when comparing databases and generating scripts. Environments can be specified by adding environment
definitions to the `flyway.user.toml` file, as the example below shows:

```toml
[environments.development]
url = "jdbc:sqlserver://localhost;authentication=sqlPassword;databaseName=Dev;encrypt=true;trustServerCertificate=true"
user = "sa"
password = "..."

[environments.shadow]
url = "jdbc:sqlserver://localhost;authentication=sqlPassword;databaseName=Shadow;encrypt=true;trustServerCertificate=true"
user = "sa"
password = "..."
provisioner = "clean"

[environments.prod]
url = "jdbc:sqlserver://localhost;authentication=sqlPassword;databaseName=Prod;encrypt=true;trustServerCertificate=true"
user = "sa"
password = "..."
```

These environments allow us to follow the usual development -> shadow -> prod workflow, where the shadow environment is
used to validate development changes before they are applied to the prod environment.

## Updating the schema model

The first step in the Flyway Desktop migrations workflow is to update the schema model with objects present in the
development database, as this allows us to get our database into version control. In order to do this, we must first
generate a diff between the development database and the schema model. This can be done using
the [diff](<Commands/Diff>) command as shown below:

```
$ flyway diff "-diff.source=development" "-diff.target=schemaModel"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-----------------------+----------------+---------------------------------------+
| Id                          | Change | Object Type           | Schema         | Name                                  |
+-----------------------------+--------+-----------------------+----------------+---------------------------------------+
| YoZgVMdZR3p7FZEygVaRX9MoF2w | Add    | DDL trigger           |                | ddlDatabaseTriggerLog                 |
| ZKZljmz2_Vvl5wLmV.mczvanHzM | Add    | Extended property     |                | MS_Description                        |
| VYj3ZC0OtkZR4CbJ_JHm9BMkg_c | Add    | Full text catalog     |                | AW2016FullTextCatalog                 |
| qlJstpTbyOQ7nRXTfUvY4lnUDIA | Add    | Function              | dbo            | ufnGetAccountingEndDate               |
...
| YreyZ8E1z3onEQFgWSGvDqaDUeY | Add    | XML schema collection | Production     | ProductDescriptionSchemaCollection    |
| UJr0Z.pCcr8O5ntP1w6H9teL8kQ | Add    | XML schema collection | Sales          | StoreSurveySchemaCollection           |
+-----------------------------+--------+-----------------------+----------------+---------------------------------------+
```

The diff command creates an artifact containing all the differences between the development database and the schema
model. This artifact can then be used with other commands, such as
the [model](<Commands/Model>) command, which applies the differences in the artifact to the
schema model.

Running the `model` command updates the schema model folder:

```
$ flyway model

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Saved to schema model
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Database Triggers\ddlDatabaseTriggerLog.sql
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Extended Properties\MS_Description.sql
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Functions\dbo.ufnGetAccountingEndDate.sql
 ...
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Views\Sales.vStoreWithContacts.sql
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Views\Sales.vStoreWithDemographics.sql
```

## Generating a baseline script

Once the database is in version control, the next step is to generate a baseline script. This can be done using the
[generate](<Commands/Generate>) command. Before `generate` can be run, the prod environment
must be diffed against the shadow environment in order to determine the changes required for the baseline script.

The `diff` command to do this is shown below:

```
$ flyway diff "-diff.source=prod" "-diff.target=migrations" "-diff.buildEnvironment=shadow"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Successfully validated 0 migrations (execution time 00:00.009s)
WARNING: No migrations found. Are your locations set up correctly?
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-----------------------+----------------+---------------------------------------+
| Id                          | Change | Object Type           | Schema         | Name                                  |
+-----------------------------+--------+-----------------------+----------------+---------------------------------------+
| YoZgVMdZR3p7FZEygVaRX9MoF2w | Add    | DDL trigger           |                | ddlDatabaseTriggerLog                 |
| ZKZljmz2_Vvl5wLmV.mczvanHzM | Add    | Extended property     |                | MS_Description                        |
| VYj3ZC0OtkZR4CbJ_JHm9BMkg_c | Add    | Full text catalog     |                | AW2016FullTextCatalog                 |
| qlJstpTbyOQ7nRXTfUvY4lnUDIA | Add    | Function              | dbo            | ufnGetAccountingEndDate               |
...
| YreyZ8E1z3onEQFgWSGvDqaDUeY | Add    | XML schema collection | Production     | ProductDescriptionSchemaCollection    |
| UJr0Z.pCcr8O5ntP1w6H9teL8kQ | Add    | XML schema collection | Sales          | StoreSurveySchemaCollection           |
+-----------------------------+--------+-----------------------+----------------+---------------------------------------+
```

Note that, here the `diff.target` is set to `migrations` and not `shadow`, whilst the `diff.buildEnvironment` is set to
`shadow`. The `migrations` target will cause the diff command to provision the `shadow` environment. That is, the shadow
environment will be migrated to the latest version before being diffed. Although at the moment we have no migrations,
this is useful further down the line when a project will have migrations.

The artifact created by the diff command can now be used to generate a baseline script using the `generate` command, as
shown below:

```
$ flyway generate "-generate.types=baseline" "-generate.description=Baseline"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Using diff artifact: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
Generating baseline migration: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\B001_20241209164727__Baseline.sql
Generated: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\B001_20241209164727__Baseline.sql
 Generated configuration: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\B001_20241209164727__Baseline.sql.conf
```

Performing a diff between the prod environment and shadow environment will no longer show any differences, as the shadow
environment will be provisioned with the baseline script that was just created:

```
$ flyway diff "-diff.source=prod" "-diff.target=migrations" "-diff.buildEnvironment=shadow"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
...
Successfully applied 1 migration to schema [dbo], now at version v001.20241209164727 (execution time 00:02.648s)
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+------+
| Id                          | Change | Object Type | Schema | Name |
+-----------------------------+--------+-------------+--------+------+
| No differences found                                               |
+-----------------------------+--------+-------------+--------+------+
```

## Generating versioned and undo migrations scripts

Generating versioned and undo scripts uses the `diff`, `model` and `generate` commands seen above. Let's
assume the following table is added to the development database:

```sql
CREATE TABLE Dev.dbo.NewTable
(
    Id   INT PRIMARY KEY,
    Name NVARCHAR(255) NOT NULL
);
```

We now want to get this change into a migration script that can be applied against production. The first step is to get
this change into the schema model. If adding the table is the only change in the development database, then we can
update the schema model with a single command by combining the `diff` and `model` commands:

```
$ flyway diff model "-diff.source=development" "-diff.target=schemaModel"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+----------+
| Id                          | Change | Object Type | Schema | Name     |
+-----------------------------+--------+-------------+--------+----------+
| J7JsJA_jmgaSrCD.hAdkbUqxxQs | Add    | Table       | dbo    | NewTable |
+-----------------------------+--------+-------------+--------+----------+

Saved to schema model
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Tables\dbo.NewTable.sql
```

i.e. The `diff` command is run first followed by the `model` command. This is equivalent to running the following
commands sequentially:

```
$ flyway diff "-diff.source=development" "-diff.target=schemaModel"
$ flyway model
```

Versioned and undo scripts can now be generated using a `diff` and `generate` command combination, where the differences
between the schema model and the shadow environment are converted into a migration script that can be executed.

```
$ flyway diff generate "-diff.source=schemaModel" "-diff.target=migrations" "-diff.buildEnvironment=shadow" "-generate.types=versioned,undo" "-generate.description=NewTableAdded"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Successfully applied 1 migration to schema [dbo], now at version v001.20241209164727 (execution time 00:02.820s)
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+----------+
| Id                          | Change | Object Type | Schema | Name     |
+-----------------------------+--------+-------------+--------+----------+
| J7JsJA_jmgaSrCD.hAdkbUqxxQs | Add    | Table       | dbo    | NewTable |
+-----------------------------+--------+-------------+--------+----------+

Using diff artifact: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
Generating versioned migration: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\V002_20241210110201__NewTableAdded.sql
Generating undo migration: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\U002_20241210110201__NewTableAdded.sql
Generated: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\U002_20241210110201__NewTableAdded.sql
Generated: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\V002_20241210110201__NewTableAdded.sql
```

We can validate the newly generated migration script by running the diff command and checking there are no
differences between the schema model and the shadow database after applying the migration (remember that the diff
command will provision the shadow environment with the latest migration scripts when `-diff.target=migrations`):

```
$ flyway diff "-diff.source=schemaModel" "-diff.target=migrations" "-diff.buildEnvironment=shadow"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Successfully validated 3 migrations (execution time 00:00.016s)
Current version of schema [dbo]: 001.20241209164727
Migrating schema [dbo] to version "002.20241210110201 - NewTableAdded"
Successfully applied 1 migration to schema [dbo], now at version v002.20241210110201 (execution time 00:00.028s)
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+------+
| Id                          | Change | Object Type | Schema | Name |
+-----------------------------+--------+-------------+--------+------+
| No differences found                                               |
+-----------------------------+--------+-------------+--------+------+
```

## Applying specific changes

The `generate` and `model` command also accept a `changes` argument, which allows a command separated list of change IDs
to be passed in as an argument or over stdin.

The example below shows how changes applied to the schema model can be limited to a specific set passed in as a CLI
argument. The `diff` command shows there are 3 tables in the development database which are not present in the schema
model. The model command below only applies the changes for the first two tables by specifying their IDs using the
`-model.changes` argument.

```
$ flyway diff "-diff.source=development" "-diff.target=schemaModel"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+------------+
| Id                          | Change | Object Type | Schema | Name       |
+-----------------------------+--------+-------------+--------+------------+
| rlWm41EHCIp8krJRqZ.PTVIx2fs | Add    | Table       | dbo    | TestTable1 |
| QvJIORGzbAAz3irDJbEKBcQr8QY | Add    | Table       | dbo    | TestTable2 |
| n6OaP76s3hsLYT8jwdpnl5.WaJ8 | Add    | Table       | dbo    | TestTable3 |
+-----------------------------+--------+-------------+--------+------------+

$ flyway model "-model.changes=rlWm41EHCIp8krJRqZ.PTVIx2fs,QvJIORGzbAAz3irDJbEKBcQr8QY"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Saved to schema model
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Tables\dbo.TestTable1.sql
 File updated: C:\Users\Flyway\FlywayProjects\SqlServerProject\schema-model\Tables\dbo.TestTable2.sql
```

Change IDs can also be passed in over stdin by passing `-` as the argument to the `changes` argument. The example
below shows how this is done for the `generate` command:

```
$ flyway diff "-diff.source=schemaModel" "-diff.target=migrations" "-diff.buildEnvironment=shadow"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)
Flyway permit on disk is outdated and can't be refreshed automatically because there is no refresh token on disk. Please rerun auth

See release notes here: https://rd.gt/416ObMi
Successfully validated 3 migrations (execution time 00:00.022s)
diff artifact generated: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
+-----------------------------+--------+-------------+--------+------------+
| Id                          | Change | Object Type | Schema | Name       |
+-----------------------------+--------+-------------+--------+------------+
| rlWm41EHCIp8krJRqZ.PTVIx2fs | Add    | Table       | dbo    | TestTable1 |
| QvJIORGzbAAz3irDJbEKBcQr8QY | Add    | Table       | dbo    | TestTable2 |
+-----------------------------+--------+-------------+--------+------------+

$ echo "rlWm41EHCIp8krJRqZ.PTVIx2fs" | flyway generate "-generate.changes=-" "-generate.types=versioned,undo" "-generate.description=TestTable1"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
Using diff artifact: C:\Users\Flyway\AppData\Local\Temp\flyway.artifact.diff
Generating undo migration: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\U003_20241210113916__TestTable1.sql
Generating versioned migration: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\V003_20241210113916__TestTable1.sql
Generated: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\V003_20241210113916__TestTable1.sql
Generated: C:\Users\Flyway\FlywayProjects\SqlServerProject\migrations\U003_20241210113916__TestTable1.sql
```

The diff for each change ID can be viewed using the [diffText](<Usage/Command-line/Command-line - diffText>) command.
For example, to view the diff for `TestTable3` we can run the following command, which uses the change ID for
`TestTable3`:

```
$ flyway diffText "-diffText.changes=n6OaP76s3hsLYT8jwdpnl5.WaJ8"

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi
--- none
+++ Table/dbo.TestTable3
CREATE TABLE [dbo].[TestTable3]
(
[Id] [int] NOT NULL,
[Name] [nvarchar] (255) NOT NULL
)
GO
ALTER TABLE [dbo].[TestTable3] ADD CONSTRAINT [PK__TestTabl__3214EC07250C0CE5] PRIMARY KEY CLUSTERED ([Id])
GO
```

## Running migration scripts on prod

Finally, we can run the migrations scripts generated on the prod environment using the `migrate` command. Note that
`-baselineOnMigrate=true` is passed to baseline the prod database as the schema history table does not exist yet.

```
flyway migrate -baselineOnMigrate=true -environment=prod

Flyway Enterprise Edition {{ site.flywayVersion }} by Redgate
Licensed to red-gate.com (license ID 1174ed6b-b10e-41bd-9a1b-285ddc3239c7)

See release notes here: https://rd.gt/416ObMi

Flyway Pipelines are not active for this project. Learn more here: https://flyway.red-gate.com
Schema history table [Prod2].[dbo].[flyway_schema_history] does not exist yet
Successfully validated 5 migrations (execution time 00:00.040s)
Creating Schema History table [Prod2].[dbo].[flyway_schema_history] with baseline ...
Successfully baselined schema with version: 1
Current version of schema [dbo]: 1
Migrating schema [dbo] to version "002.20241210110201 - NewTableAdded"
Migrating schema [dbo] to version "003.20241210113916 - TestTable1"
Successfully applied 2 migrations to schema [dbo], now at version v003.20241210113916 (execution time 00:00.023s)
```