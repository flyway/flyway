---
subtitle: MongoDB - Native Connectors
---

- **Verified Versions:** V5,V8
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Terminology
We have to map Flyway concepts and language rooted in the relational database world to MongoDB - this is how Flyway sees the mapping:

| MongoDB Concept | Flyway Concept  |
| --------------- |-----------------|
| database        | database/schema |
| collection      | table           |
| document        | row             |
| transaction     | transaction     |

## Configuration
- Flyway identifies the migration type through the file extension. You should set [sqlMigrationSuffixes](<Configuration/Flyway Namespace/Flyway SQL Migration Suffixes Setting>) to `.js` for JavaScript migrations or `.json` for JSON migrations.
   - Note that Flyway doesn't support mixed migration types in a single project 
- Any non-credential configuration (see [Mongo Connection String Options](https://www.mongodb.com/docs/manual/reference/connection-string-options/) needs to go into the connection string (Flyway's [URL](Configuration/Environments Namespace/Environment URL Setting) parameter), as this is passed directly to both the driver and Mongosh. 
   - Credentials can be provided via Flyway's standard [user](<Configuration/Environments Namespace/Environment User Setting>) and [password](<Configuration/Environments Namespace/Environment Password Setting>) parameters
   - If you are not using the default database, you will need to include the database name and auth source in your url.
- There is a [tutorial available here](/tutorials/tutorial---using-native-connectors-to-connect-to-mongodb).

### JavaScript migrations
- JavaScript migrations require [`mongosh`](https://www.mongodb.com/docs/mongodb-shell/install/) to be installed where Flyway can use it. 
- To use JavaScript migrations with our official Docker image, you will need to use either the `redgate/flyway:{{site.flywayVersion}}-mongo`, `-alpine-mongo` or `-azure-mongo` images as these include the Mongosh tool.


## Limitations

- JavaScript (`.js`) migrations cannot use transactions, which is a limitation of Mongosh. A warning will be displayed if `executeInTransaction` is set.
See [this blog post](https://documentation.red-gate.com/display/FD/Flyway+Native+Connectors+-+MongoDB) for more details.
- You can't currently do a [Dry-run](<https://documentation.red-gate.com/fd/migration-command-dry-runs-275218517.html>) operation with MongoDB.
