---
subtitle: Add
---

## Description

The `add` command is used to create new migration scripts following the naming convention of your existing migrations.

If no version is provided to the add command then Flyway will search all configured filesystem migration locations to determine the next migration version.

In the case where an undo migration is generated then Flyway will use the description and version of the highest versioned migration.

## Usage examples

### Generating a versioned migration script

If the migrations directory has the following contents before running the add command:
```
B001__baseline.sql
V002_20240829162505__feature_A.sql
```
Then the following `add` command will create a new migration script with an auto-generated version:
<pre class="console">&gt; flyway add -description=feature_B

Flyway {{ site.flywayVersion }} by Redgate

Database: jdbc:mysql://clone-internal.red-gate.com:32781/mysql (MySQL 8.0)
created: .\migrations\V003_20240829163045__feature_B.sql
</pre>

Leaving the migrations directory with the following contents:
```
B001__baseline.sql
V002_20240828142505__feature_A.sql
V003_20240829163045__feature_B.sql
```

**Note**: Flyway uses the latest version in the migrations folder to calculate the next migration version.
If a timestamp of the format `yyyyMMddHHmmss` is part of the version then this will also be updated, as can be seen above.

### Generating an undo migration script

If the migrations directory has the following contents before running the add command:
```
B001__baseline.sql
V002__feature_A.sql
```
Then the following `add` command will create a new undo migration script using the version and description of the most recent versioned migration:
<pre class="console">&gt; flyway add -type=undo

Flyway {{ site.flywayVersion }} by Redgate

Database: jdbc:mysql://clone-internal.red-gate.com:32781/mysql (MySQL 8.0)
Database: jdbc:mysql://clone-internal.red-gate.com:32781/mysql (MySQL 8.0)
created: .\migrations\U002__feature_A.sql
</pre>

Leaving the migrations directory with the following contents:
```
B001__baseline.sql
U002__feature_A.sql
V002__feature_A.sql
```

### Generating a baseline migration script

In this case the migrations directory will be empty, as the baseline migration is the first script to be created.
Running the following `add` command will add a baseline script at the specified location and defaulting to version `001`:
<pre class="console">&gt; flyway add -type=baseline -location=C:\Users\FlywayUser\Project\migrations\ -description=initial_state

Flyway {{ site.flywayVersion }} by Redgate

Database: jdbc:mysql://clone-internal.red-gate.com:32781/mysql (MySQL 8.0)
created: C:\Users\FlywayUser\Project\migrations\B001__initial_state.sql
</pre>

Leaving the migrations directory with the following contents:
```
B001__initial_state.sql
```

### Generating only the migration script name

Using the `-q` option in combination with the `add.nameOnly` option, it's possible for flyway to only print the name of the migration script that would be created.
For example, the following command would open the next migration script in the `vim` editor when run in a `bash` shell, without creating the file:
<pre class="console">&gt; vim $(flyway -q add -add.nameOnly=true)</pre>

## Parameters

### Optional

| Parameter                                                                                                        | Namespace | Description                                                                    |
|------------------------------------------------------------------------------------------------------------------|-----------|--------------------------------------------------------------------------------|
| [`type`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add Type Setting>)               | add       | The type of migration to create.                                               |
| [`description`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add Description Setting>) | add       | The description part of the migration name.                                    |
| [`version`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add Version Setting>)         | add       | The version part of the migration name.                                        |
| [`location`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add Location Setting>)       | add       | The location to generate the migration to.                                     |
| [`nameOnly`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add name Only Setting>)      | add       | Only return the name of the migration script, without creating the empty file. |
| [`force`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add Force Setting>)             | add       | If the file already exists, overwrite it.                                      |
| [`timestamp`](<Configuration/Flyway Namespace/Flyway Add Namespace/Flyway Add Timestamp Setting>)     | add       | Add a timestamp to the calculated version if one is not already present.       |

Universal commandline parameters are listed [here](<Command-line Parameters>).

## Error codes

This command can produce the following error codes:
- [Generic error codes](<Exit codes and error codes/General error codes>)
