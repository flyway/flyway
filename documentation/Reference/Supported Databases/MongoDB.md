---
subtitle: MongoDB
---

# MongoDB - Preview

- **Verified Versions:** V7
- **Maintainer:** {% include redgate-badge.html %}
- **JDBC Driver:** JetBrains Datagrip MongoDB Driver.
    - Binaries: [here](https://www.jetbrains.com/datagrip/jdbc-drivers/),
    - Source: [here](https://github.com/DataGrip/mongo-jdbc-driver)

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Native Connectors

If you are on the OSS edition, you will have Native Connectors on by default. Otherwise, you may have switched it on.

If you have, you should consult the [MongoDB - Native Connectors](/supported-databases/mongodb/mongodb---native-connectors) documentation instead of this page.

## Driver

| Item                               | Details                                                                                                                                 |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:mongodb://<i>ip address:port number/database_name</i></code> <code>jdbc:mongodb+srv://<i>ip address/database_name</i></code> |
| **SSL support**                    | No                                                                                                                                      |
| **Ships with Flyway Command-line** | Yes (Redgate Distribution only while in preview)                                                                                        |
| **Maven Central coordinates**      | n/a                                                                                                                                     |
| **Supported versions**             | `7` and later (including Mongo Atlas)                                                                                                   |
| **Default Java class**             | `com.dbschema.MongoJdbcDriver`                                                                                                          |

## Terminology
We have to map Flyway concepts and language rooted in the relational database world to MongoDB - this is how Flyway sees the mapping:
  
| MongoDB Concept | Flyway Concept  |
|-----------------|-----------------|
| database        | database/schema |
| collection      | table           |
| row             | document        |
| transaction     | transaction     |

## Using Flyway with MongoDB

### Pre-requisites

- Using Flyway with Maven?
    - Include the latest Flyway MongoDB dependency [here](https://central.sonatype.com/artifact/org.flywaydb/flyway-database-mongodb) in your pom
        - For example:
            - Redgate
              ```xml
              <dependency>
                  <groupId>com.redgate.flyway</groupId>
                  <artifactId>flyway-database-mongodb</artifactId>
              </dependency>
              ```
            - Open Source
              ```xml
              <dependency>
                  <groupId>org.flywaydb</groupId>
                  <artifactId>flyway-database-mongodb</artifactId>
              </dependency>
              ```
    - Include the latest MongoDB driver dependency [here](https://github.com/DataGrip/mongo-jdbc-driver) in your buildscript or import into your local maven repository:
        - For example:
          ```xml
              <dependency>
                  <groupId>com.github.kornilova203</groupId>
                  <artifactId>mongo-jdbc-driver</artifactId>
                  <version>1.19</version>
                  <scope>system</scope>
                  <systemPath>mongo-jdbc-standalone-1.19.jar</systemPath>  
              </dependency>
          ```
        - or `mvn install:install-file -Dfile=mongo-jdbc-standalone-1.19.jar -DgroupId=com.github.kornilova203 -DartifactId=mongo-jdbc-driver -Dversion=1.19 -Dpackaging=jar -DgeneratePom=true`

- Using Flyway with Gradle?
    - Include the latest Flyway MongoDB dependency [here](https://central.sonatype.com/artifact/org.flywaydb/flyway-database-mongodb) as a buildscript dependency
    - Include the latest MongoDB driver dependency [here](https://github.com/DataGrip/mongo-jdbc-driver) in your buildscript
        - For example:
      ```groovy
        dependencies {
          implementation files('mongo-jdbc-standalone-1.19.jar')
        }
      ```

### Configuring Flyway

You must configure a JDBC URL that points to your database. You can configure a connection using this sample URL as an example:

<code>jdbc:mongodb://<i>ip address</i>:<i>port number</i>/<i>database_name</i></code>

We need to fetch three things to complete this url:

- `ip address`
- `port number` _(optional)_
- `database_name` _(optional)_

### MongoDB Scripts

Migrations in mongo are not `.sql` files like the flyway norm, but instead are `.js`. With standard configuration, flyway will still look for `.sql` files as migrations, but when executing them will expect them to contain `javascript`.

To make this more of a native Mongo experience, we recommend changing the [sqlMigrationSuffixes](<Configuration/Flyway Namespace/Flyway SQL Migration Suffixes Setting>) configuration to `.js`. This can be done in your TOML configuration as such:

```
[flyway]
sqlMigrationSuffixes = [".js"]
```

### Limitations

- You can't currently do a [Dry-run](https://documentation.red-gate.com/flyway/flyway-concepts/migrations/migration-comand-dry-runs) operation with MongoDB.

### Known issues

- Performance connecting to MongoDB can be slow. This is a known issue in the driver we are using. It should only impact initial connection so will not impact larger or many migrations. We are investigating solutions to this.
- When you see the output of flyway, the migration will be described as type `SQL`. Don't worry, this is just how Flyway describes a versioned migration - your migration hasn't been changed on its way through Flyway ! 

### MongoDB preview feedback

Thank you for testing this preview feature. If you come across any issues or want to contribute other feedback, please either raise an issue on our [github issue tracker](https://github.com/flyway/flyway/issues) or drop us an email at [DatabaseDevOps@red-gate.com](mailto:DatabaseDevOps@red-gate.com)
