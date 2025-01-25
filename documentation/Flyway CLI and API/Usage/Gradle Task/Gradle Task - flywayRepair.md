---
pill: gradle_repair
subtitle: 'gradle flywayRepair'
---
# Gradle Task: flywayRepair

Repairs the Flyway schema history table. This will perform the following actions:
- Remove any failed migrations<br/>
  (User objects left behind must still be cleaned up manually)
- Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations

<a href="Commands/repair"><img src="assets/command-repair.png" alt="repair"></a>

## Usage

<pre class="console">&gt; gradle flywayRepair</pre>

## Configuration

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

## Sample output

<pre class="console">&gt; gradle flywayRepair -i

Repair not necessary. No failed migration detected.</pre>
