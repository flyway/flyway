---
subtitle: Derby
---

## Supported Versions

- **Verified Versions:** 10.11, 10.15 (Important: see 'Compatibility' section below)
- **Maintainer:** {% include redgate-badge.html %}

## Supported Versions and Support Levels

{% include database-boilerplate.html %}

## Driver

| Item                               | Details                                                          |
|------------------------------------|------------------------------------------------------------------|
| **URL format**                     | <code>jdbc:derby:<i>sub-protocol</i>:<i>database name</i></code> |
| **Ships with Flyway Command-line** | No                                                               |
| **Maven Central coordinates**      | `org.apache.derby:derbyclient`                                   |
| **Supported versions**             | `10.11` and later                                                |
| **Default Java class**             | `org.apache.derby.jdbc.EmbeddedDriver`                           |

## CLI Usage - installing support

If you want to connect to a Derby database you will first need to download the driver that is compatible with Java 17 from the [Derby download page](https://db.apache.org/derby/derby_downloads.html)
Unpack and place the following files in a location Flyway can find:

* `derby-<verion>.jar`
* `derbyclient-<verion>.jar`
* `derbyshared-<verion>.jar`
* `derbytools-<verion>.jar`

The location needs to be on the [Flyway classpath](<Usage/Adding to the classpath>), we would recommend using the
[jarDirs](<Configuration/Flyway Namespace/Flyway Jar Dirs Setting>) parameter, if you keep these libraries outside your Flyway installation
then it will make future updates easier.

## Java Usage

Derby support is a separate dependency for Flyway and will need to be added to your Java project to access these features.
Derby is found within the `flyway-database-derby` plugin module.

### Maven

#### Redgate

```xml
<dependency>
    <groupId>com.redgate.flyway</groupId>
    <artifactId>flyway-database-derby</artifactId>
</dependency>
```

#### Open Source

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-derby</artifactId>
</dependency>
```

### Gradle

#### Redgate

```groovy
dependencies {
    implementation "com.redgate.flyway:flyway-database-derby"
}
```

#### Open Source

```groovy
dependencies {
    implementation "org.flywaydb:flyway-database-derby"
}
```

## SQL Script Syntax

- Standard SQL syntax with statement delimiter **;**

### Compatibility

- DDL exported by Derby can be used unchanged in a Flyway migration
- Any Derby SQL script executed by Flyway, can be executed by the Derby tools (after the placeholders have been replaced)
- The Derby 10.15 driver requires Java 9+. Flyway users who are constrained to use Java 8 should **not** upgrade to Derby 10.15.

### Example

```sql
/* Single line comment */
CREATE TABLE test_data (
  value VARCHAR(25) NOT NULL PRIMARY KEY
);

/*
Multi-line
comment
*/

-- Sql-style comment

-- Placeholder
INSERT INTO ${tableName} (name) VALUES ('Mr. T');
```

## Limitations

- *None*
