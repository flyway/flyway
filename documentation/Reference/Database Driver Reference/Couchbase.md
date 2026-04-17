---
subtitle: Couchbase
---

- **Status:** {% include preview.html %}
- **Verified Versions:** V7.6.6-N1QL
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                            |
|------------------------------------|----------------------------------------------------|
| **URL format**                     | `couchbases://cb.<cluster-id>.cloud.couchbase.com` |
| **SSL support**                    | No                                                 |
| **Ships with Flyway Command-line** | Yes                                                |
| **Maven Central coordinates**      | n/a                                                |
| **Supported versions**             | `7`                                                |
| **Default Java class**             | `com.couchbase.client`                             |

## Terminology
We have to map Flyway concepts and language rooted in the relational database world to Couchbase - this is how Flyway sees the mapping:
  
| Couchbase Concept | Flyway Concept |
|-------------------|----------------|
| bucket + scope    | schema         |
| collection        | table          |
| document          | row            |

## Using Flyway with Couchbase

### Configuring Flyway

Since Couchbase has no default schema, you must define at least one schema, using either the [`defaultSchema`](<Configuration/Flyway Namespace/Flyway Default Schema Setting>) parameter or the [schemas](<Configuration/Environments Namespace/Environment schemas Setting>) parameter.

The schema can be specified in two ways:

- As {String}, Flyway interprets this string as the `bucket` name. In this case, Flyway uses `_default` as the default `scope`.

- As {String.String}, Flyway splits the value at the `.` character, treating the first part as the `bucket` name and the second part as the `scope` name.

Couchbase does not support embedding credentials (username and password) in the connection URL. Credentials must instead be provided separately through parameters in the Flyway Environment namespace.

```toml
[environments.sample]
url = "couchbases://cb.<cluster-id>.cloud.couchbase.com"
user = "user"
password = "password"

[flyway]
environment = "sample"
defaultSchema = "bucket.scope"
```

### Limitations

- You can't currently do a [Dry-run](<https://documentation.red-gate.com/fd/migration-command-dry-runs-275218517.html>) operation with Couchbase.
- Currently, Flyway allows only one statement per migration. Using multiple statements within a single migration file is not supported
- Schema creation and drop. Flyway currently supports creating and dropping `scope`, but does not support creating or dropping `bucket`. 

### Considerations

- Transactions are supported; however, not all statements in Couchbase can be executed within a transaction. Flyway does not attempt to automatically group migrations. When the [group](<onfiguration/Flyway Namespace/Group>) parameter is enabled, it is the user’s responsibility to ensure that all statements are transaction-compatible.
- Currently, when connecting via the Flyway command line, a warning message related to `SLF4J binding` may appear. This is a known issue and is under investigation.
- Couchbase is supported only in `Native Connectors` mode. If the environment variable `FLYWAY_NATIVE_CONNECTORS` is set to `false` then Flyway will be unable to operate and will output an error "No Flyway database plugin found to handle ...".
- Foundational support is currently in preview and has been verified only in cloud deployments. If you encounter any issues when using it with on-premise deployments, please report them through any convenient channel.
