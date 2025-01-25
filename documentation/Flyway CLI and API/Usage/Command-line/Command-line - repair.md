---
pill: cli_repair
subtitle: 'Command-line: repair'
---
# Command-line: repair

Repairs the Flyway schema history table. This will perform the following actions:
- Remove any failed migrations<br/>
            (User objects left behind must still be cleaned up manually)
- Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations
- Mark all missing migrations as **deleted**
    - As a result, `repair` must be given the same [`locations`](Configuration/parameters/flyway/locations) as `migrate`!

<a href="Commands/repair"><img src="assets/command-repair.png" alt="repair"></a>

## Usage

<pre class="console"><span>&gt;</span> flyway [options] repair</pre>

## Options

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

## Sample output

<pre class="console">&gt; flyway repair

Flyway {{ site.flywayVersion }} by Redgate

Repair not necessary. No failed migration detected.</pre>

## Sample JSON output

<pre class="console">&gt; flyway repair -outputType=json

{
  "repairActions": [
    "ALIGNED APPLIED MIGRATION CHECKSUMS"
  ],
  "migrationsRemoved": [],
  "migrationsDeleted": [],
  "migrationsAligned": [
    {
      "version": "1",
      "description": "first",
      "filepath": "C:\\flyway\\sql\\V1__first.sql"
    }
  ],
  "flywayVersion": "{{ site.flywayVersion }}",
  "database": "testdb",
  "warnings": [],
  "operation": "repair"
}</pre>
