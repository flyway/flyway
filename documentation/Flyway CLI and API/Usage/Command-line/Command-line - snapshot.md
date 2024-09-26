---
pill: cli_snapshot
subtitle: 'Command-line: snapshot'
---
# Command-line: snapshot

{% include enterprise.html %}

Generates a snapshot from a database environment, build environment, schema model folder or empty source.
A snapshot captures the schema of a specified source into a file for subsequent use with the
[Check command](<Commands/Check Command>), [Diff command](<Concepts/Diff concept>) or [Snapshot provisioner](Configuration/Provisioners/Snapshot Provisioner).

<img src="assets/command-snapshot.png" alt="snapshot">

## Usage

<pre class="console"><span>&gt;</span> flyway snapshot [options]</pre>

## Options

The following options can be provided to the `snapshot` command in the format -key=value:
- `snapshot.filename` [REQUIRED] - Destination filename for the snapshot 
- `snapshot.source` (preview) - The source from which a snapshot should be generated. Valid values:
    - `<<env>>` - uses the environment named `<<env>>`
    - `empty` - models an empty database
    - `schemaModel` - the schema model folder referenced by schemaModelLocation
    - `migrations` - uses a buildEnvironment to represent the state of database after specified migrations have been applied
- `snapshot.buildEnvironment` (preview) - If source is migrations, this specifies the environment to use as the build environment
- `snapshot.buildVersion` (preview) - If source is migrations, this specifies migration version to migrate the build environment to
- `snapshot.buildCherryPick` (preview) - If source is migrations, this specifies list of migrations to migrate the build environment with
- `snapshot.rebuild` (preview) - If source is migrations, forces a reprovision (rebuild) of the build environment

## Examples

### Generating a snapshot from a database URL

The following command generates a snapshot from a database URL:

<pre class="console">&gt; flyway snapshot -url=jdbc:sqlserver://localhost:1433;encrypt=false;databaseName=Inventory -user=sa -password=... -snapshot.filename=C:\snapshot.json

Flyway {{ site.flywayVersion }} by Redgate

Result of snapshot written to C:\snapshot.json
</pre>

### Generating a snapshot from a database environment

Equivalently, if a database environment named `dev` is configured in the `flyway.toml` file, then the environment name
can be provided as a CLI argument instead of passing the connection details as CLI arguments.
For example, the following command generates a snapshot from the `dev` environment:
<pre class="console">&gt; flyway snapshot -snapshot.source=dev -snapshot.filename=C:\snapshot.json

Flyway {{ site.flywayVersion }} by Redgate

Result of snapshot written to C:\snapshot.json
</pre>

### Generating a snapshot from a schema model folder

A snapshot can be generated from a schema model folder.
For example, the following command generates a snapshot from the schema model folder:
<pre class="console">&gt; flyway snapshot -snapshot.source=schemaModel -snapshot.filename=C:\snapshot.json

Flyway {{ site.flywayVersion }} by Redgate

Result of snapshot written to C:\snapshot.json
</pre>
**Note**: The `schemaModelLocation` property must be configured in the `flyway.toml` file.

### Generating a snapshot using the build database

A snapshot can be generated of a specific migration version by using the build database with the snapshot command.
For example, the following command migrates the specified build environment to version 2 and then takes a snapshot of the build database.
The `snapshot.buildEnvironment` argument specifies the name of the build database environment and the
`snapshot.buildVersion` argument specifies the migration version to provision the build environment to.

<pre class="console">&gt; flyway snapshot -snapshot.source=migrations -snapshot.buildEnvironment=shadow -snapshot.buildVersion=2 -snapshot.filename=C:\snapshot.json

Flyway {{ site.flywayVersion }} by Redgate

Successfully validated 3 migrations (execution time 00:00.008s)
Current version of schema [dbo]: << Empty Schema >>
Migrating schema [dbo] to version "1 - first"
Migrating schema [dbo] to version "2 - second"
Successfully applied 2 migrations to schema [dbo], now at version v2 (execution time 00:00.013s)
Result of snapshot written to C:\snapshot.json
</pre>

The `snapshot.build*` arguments make it possible to create a snapshot for any migration version.

