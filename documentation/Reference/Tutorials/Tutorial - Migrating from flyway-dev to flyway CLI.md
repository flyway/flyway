---
subtitle: 'Tutorial: Migrating from flyway-dev to flyway CLI'
---

## Introduction

The V11 release of Flyway saw several new Flyway CLI verbs removed from preview and officially released. In this
document we will explore how to migrate from `flyway-dev` to these new verbs, as `flyway-dev` will eventually be
deprecated in favour of flyway CLI.

This document will go through the key `flyway-dev` commands and show how to achieve the same functionality using the
new flyway CLI verbs.

## Prerequisites

The examples presented here assumed you are using at least Flyway CLI version 11.0.0 and have a teams or enterprise
license.

*Note*: The flyway CLI examples below assume flyway is run from the project directory. To run flyway CLI from another
directory use the [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>) argument to specify the
project location.

## init - Creating a new project

The `init` command initializes a new flyway project, For examples, a new SQL Server project can be created using
the `flyway-dev init` command as shown below:

```
flyway-dev init --name=SqlServerProject --path=C:\Users\redgate\Documents\SqlServerProject --database-type=SqlServer --i-agree-to-the-eula
```

Flyway CLI also has an `init` command which performs the same action, and so the equivalent can be achieved using the
`init` command in flyway CLI as shown below. However, note that flyway CLI must be run from the directory where the
project should be created.

```
$ mkdir C:\Users\redgate\Documents\SqlServerProject
$ cd C:\Users\redgate\Documents\SqlServerProject
$ flyway init "-init.projectName=SqlServerProject" "-init.databaseType=sqlserver"
```

## diff - Calculate the differences between two sources

The `diff` command is used to determine the differences between two sources, so that the differences can later be
applied to a target or used to generate a migration script. For example, to generate a diff between the dev environment
and schemaModel, the following `flyway-dev` command can be used:

```
flyway-dev diff -p=C:\Users\redgate\Documents\TestFdCmdLine --from=Dev --to=SchemaModel -a=C:\Users\redgate\Documents\TestFdCmdLine\Temp\artifact1 --i-agree-to-the-eula
```

Flyway CLI also has a [diff](<Commands/Diff>) command which performs the same action, and takes
a variety of sources to compare between:

- `envId` - uses the environment with the id `envId`, defined in the TOML configuration
- `empty` - models an empty database
- `schemaModel` - the schema model folder referenced by schemaModelLocation
- `migrations` - uses a buildEnvironment to represent the state of database after specified migrations have been applied
- `snapshot:<filePath>` - uses a snapshot file at the specified file path

Therefore, to perform the same comparison between the dev environment and schemaModel using flyway CLI, the following
command can be used:

```
flyway diff "-diff.source=development" "-diff.target=schemaModel"
```

Note:

- The id of the environment should be used as an argument to `diff.source` or `diff.target` in flyway CLI.
- A path to the artifact file can optionally be provided using the `-diff.artifactFilename` argument, however a default
  path in a temporary location will be used if none is provided.

## show - Display changes in a diff artifact

The `show` command is used to display the changes in a diff artifact. For example, the following `flyway-dev` command
will display the differences from the specified artifact file:

```
flyway-dev show -p=C:\Users\redgate\Documents\TestFdCmdLine -a=C:\Users\redgate\Documents\TestFdCmdLine\Temp\artifact1 --i-agree-to-the-eula
```

Flyway CLI has a [diffText](<Commands/DiffText>) command which performs the same action, and
is invoked in a similar way:

```
flyway diffText
```

*Note*: `diffText` will use the default artifact location if none is provided. An artifact file can be specified using
the`-diffText.artifactFilename` argument.

## apply - Apply changes from a diff artifact to a target

The `apply` command is used to apply changes from a diff artifact to a target, where the target could be the schema
model or a target database. For example, the following `flyway-dev` command will apply the specified change from the
artifact file to the target used in the diff:

```
flyway-dev apply -p=C:\Users\redgate\Documents\TestFdCmdLine -a=C:\Users\redgate\Documents\TestFdCmdLine\Temp\artifact1 -c=VGFibGU6W2Rib10uW1Rlc3RUYWJsZV0 --i-agree-to-the-eula
```

Flyway CLI doesn't have an exact equivalent to the `apply` command. However, it's possible to apply changes from a diff
artifact to the schema model or a target database using the `model` command or `prepare + deploy` commands respectively.
Each case is examined below.

### Apply changes to the schema model

The [model](<Commands/Model>) command can be used to apply changes from a diff artifact to the
schema model. The example below applies the specified change from the diff artifact to the schema model:

```
flyway model "-model.changes=VGFibGU6W2Rib10uW1Rlc3RUYWJsZV0" "-model.artifactFilename=C:\Users\redgate\Documents\TestFdCmdLine\Temp\artifact1"
```

### Apply changes to a target database

The [prepare](<Commands/Prepare>) and [deploy](<Commands/Deploy>) commands can be used in combination to apply changes
to a target database. The example below applies the specified change from the diff artifact to the prod environment:

```
flyway prepare deploy "-prepare.artifactFilename=C:\Users\redgate\Documents\TestFdCmdLine\Temp\artifact1" "-prepare.changes=VGFibGU6W2Rib10uW1Rlc3RUYWJsZV0" -environment=prod
```

## generate - Generate a migration script from a diff artifact

The `generate` command is used to generate a migration script from a diff artifact. For example, the following
`flyway-dev` command will generate a baseline migration script from the specified artifact file:

```
echo "VGFibGU6W2Rib10uW1Rlc3RUYWJsZV0" | flyway-dev generate -p 'C:\WorkingFolders\FWD\ProjectFolder' -a 'C:\WorkingFolders\FWD\ProjectFolder\artifact.zip' -o 'C:\WorkingFolders\FWD\ProjectFolder\migrations' --name 'B001__BaselineScript.sql' --versioned-only --i-agree-to-the-eula 
```

Flyway CLI has a [generate](<Commands/Generate>) command which performs the same action. The
following command generates a baseline script:

```
flyway generate "-generate.changes=VGFibGU6W2Rib10uW1Rlc3RUYWJsZV0" "-generate.artifactFilename=C:\WorkingFolders\FWD\ProjectFolder\artifact.zip" "-generate.baselineFilename=B001__BaselineScript.sql" "-generate.types=baseline"
```

Versioned and undo scripts can also be generated in a similar way:

```
flyway generate "-generate.changes=QvJIORGzbAAz3irDJbEKBcQr8QY" "-generate.types=versioned,undo" "-generate.description=NewFeature"
```

## next-migration-name - Generate the next migration name

The `next-migration-name` command is used to generate the next migration name. For example, the following `flyway-dev`
command will generate the next migration name:

```
flyway-dev next-migration-name -p=C:\Users\redgate\Documents\TestFdCmdLine -d="DataUpdates" --i-agree-to-the-eula
```

Flyway CLI has an `add` command which can be used to generate the next migration name. The following command
prints out the next migration name:

```
flyway add "-add.nameOnly=true" "-add.description=DataUpdates"
```

Note: Omitting `-add.nameOnly=true` will cause the add command to create the migration file in the first filesystem
migration location found.

The `generate` command has parameters that can be used to specify the migration filename to be created. Therefore,
generation of the migration filename can be combined with the creation of the next migration script. For example, the
`generate` command below creates a versioned script with the filename `V150_20241213111525__AddOrdersTable.sql`
containing the SQL changes from the most recent diff artifact:

```
flyway generate "-generate.types=versioned" "-generate.description=AddOrdersTable" "-generate.version=150" "-generate.timestamp=always"
```

Therefore, scenarios where `next-migration-name` could be used are now covered by the `generate` and `add` commands in
flyway CLI.

## Summary

The table below summarizes `flyway-dev` commands and their equivalent in flyway CLI:

| flyway-dev Command         | flyway CLI Command            | Notes                                                                                                 |
|----------------------------|-------------------------------|-------------------------------------------------------------------------------------------------------|
| `init`                     | `init`                        |                                                                                                       |
| `diff`                     | `diff`                        |                                                                                                       |
| `show`                     | `diffText`                    |                                                                                                       |
| `take`                     | (No equivalent)               |                                                                                                       |
| `apply`                    | `model` or `prepare + deploy` | Use `model` for deploying to a schema model and `prepare + deploy` for deploying to a target database |
| `generate`                 | `generate`                    |                                                                                                       |
| `promote`                  | (No equivalent)               |                                                                                                       |
| `resolve`                  | `info`                        | The `resolve` command is a wrapper around `flyway info`                                               |
| `parse-migration-name`     | (No equivalent)               |                                                                                                       |
| `construct-migration-name` | `add`                         |                                                                                                       |
