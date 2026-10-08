---
subtitle: Cassandra - Native Connectors
---

- **Status:** {% include preview.html %}
- **Verified Versions:** N/A
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                                                              |
|------------------------------------|------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>cassandra://<i>host</i>:<i>port</i>/<i>keyspace</i>?localdatacenter=<i>datacenter1</i></code> |
| **Ships with Flyway Command-line** | Yes                                                                                                  |
| **Maven Central coordinates**      | `org.apache.cassandra:java-driver-core`                                                              |
| **Default port**                   | 9042                                                                                                 |

## Using Flyway with Cassandra Native Connectors

Flyway's Native Connector support for Cassandra requires setting the environment variable `FLYWAY_NATIVE_CONNECTORS=true`.

Unlike the JDBC connector, the Native Connector communicates directly with Cassandra using the Java driver API rather than going through a JDBC wrapper. The legacy `jdbc:cassandra://` prefix is also accepted but deprecated.

## Good to know

### CQL file extensions

Cassandra migrations typically have a `.cql` migration suffix, we recommend configuring flyway to pick these up using the [`sqlMigrationSuffixes`](<Configuration/Flyway Namespace/Flyway SQL Migration Suffixes Setting>) Parameter.

You would specify this in your TOML configuration like this:

```
[flyway]
sqlMigrationSuffixes = [".cql"]
```

### Default schema/keyspace

Flyway maps it's concept of schema onto a keyspace in Cassandra. If a keyspace is specified in the URL, it will be used as the default schema. You can also configure it explicitly using:

- [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>)
- [`schemas`](<Configuration/Environments Namespace/Environment Schemas Setting>)

## Limitations

- You can't currently do a [Dry-run](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-command-dry-runs) on operations with Cassandra.
- You can only specify a single host in the URL (specifying multiple hosts was a feature of the legacy ING JDBC driver), the driver and database take care of connecting to other hosts in the cluster. This does mean that if the host Flyway is connecting to is unavailable then you will need to update the url to point to an available host. 
