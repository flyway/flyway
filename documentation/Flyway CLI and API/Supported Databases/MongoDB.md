---
subtitle: MongoDB
---
# MongoDB - Preview
- **Verified Versions:** V7
- **Maintainer:** Redgate
- **JDBC Driver:** Jetbrains Datagrip MongoDB Driver.
    - Binaries: [here](https://www.jetbrains.com/datagrip/jdbc-drivers/),
    - Source: [here](https://github.com/DataGrip/mongo-jdbc-driver)
  
## Supported Versions and Support Levels
For information regarding the supported version and support levels available,
please see [Supported Databases for Flyway](https://documentation.red-gate.com/flyway/learn-more-about-flyway/system-requirements/supported-databases-for-flyway)

For information regarding the Flyway features available, please see [Flyway feature glossary](https://documentation.red-gate.com/flyway/learn-more-about-flyway/feature-glossary)

## Driver

| Item                               | Details                                                                                                                                                             |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:mongodb://<i>ip address</i>:<i>port number>/<i>database_name</i></code> <code>jdbc:mongodb+srv://<i>ip address</i>database_name</i></code> |
| **SSL support**                    | No                                                                                                                                                                 |
| **Ships with Flyway Command-line** | Yes (Redgate Distrubution only while in preview)                                                                                                                                                                 |
| **Maven Central coordinates**      | n/a                                                                                                                  |
| **Supported versions**             | `7` and later (including Mongo Atlas)                                                                                                                                                  |
| **Default Java class**             | `com.dbschema.MongoJdbcDriver`                                                                                                                          |

## Using Flyway with MongoDB

### Pre-requisites
- Using Flyway with Maven?
    - Include the latest Flyway MongoDB dependency [here](https://central.sonatype.com/artifact/org.flywaydb/flyway-database-mongodb) in your pom
- Using Flyway with Gradle?
    - Include the latest Flyway MongoDB dependency [here](https://central.sonatype.com/artifact/org.flywaydb/flyway-database-mongodb) as a buildscript dependency 

### Configuring Flyway

You must configure a JDBC URL that points to your database. You can configure a connection using this sample URL as an example:

<code>jdbc:mongodb://<i>ip address</i>:<i>port number</i>/<i>database_name</i></code>

We need to fetch three things to complete this url:

- `ip address`
- `port number` _(optional)_
- `database_name` _(optional)_

### MongoDB Scripts

Migrations in mongo are not `.sql` files like the flyway norm, but instead are `.js`. With standard configuration, flyway will still look for `.sql` files as migrations, but when executing them will expect them to contain `javascript`.

To make this more of a native Mongo experience, we recommend changing the [sqlMigrationSuffixes](/flyway-cli-and-api/configuration/parameters/flyway/sql-migration-suffixes) configuration to `.js`. This can be done in your TOML configuration as such:

```
[flyway]
sqlMigrationSuffixes = [".js"]
```

### Known issues

Performance connecting to MongoDB can be slow. This is a known issue in the driver we are using. It should only impact initial connection so will not impact larger or many migrations. We are investigating solutions to this.

### MongoDB preview feedback

Thank you for testing this preview feature. If you come across any issues or want to contribute other feedback, please either raise an issue on our [github issue tracker](https://github.com/flyway/flyway/issues) or drop us an email at [DatabaseDevOps@red-gate.com](mailto:DatabaseDevOps@red-gate.com)
