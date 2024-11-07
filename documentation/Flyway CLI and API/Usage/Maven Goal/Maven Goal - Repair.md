---
pill: mvn_repair
subtitle: 'mvn flyway:repair'
---
# Maven Goal: Repair

Repairs the Flyway schema history table. This will perform the following actions:
- Remove any failed migrations<br/>
  (User objects left behind must still be cleaned up manually)
- Realign the checksums, descriptions and types of the applied migrations with the ones of the available migrations

<a href="Commands/repair"><img src="assets/command-repair.png" alt="repair"></a>

## Usage

<pre class="console"><span>&gt;</span> mvn flyway:repair</pre>

## Configuration

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

## Sample output

<pre class="console">&gt; mvn flyway:repair

[INFO] [flyway:repair {execution: default-cli}]
[INFO] Repair not necessary. No failed migration detected.</pre>
