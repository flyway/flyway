---
pill: password
subtitle: flyway.password
redirect_from: Configuration/password/
---

## Description

The password to use to connect to the database

This can be omitted if the password is baked into the [url](<Configuration/Environments Namespace/Environment url Setting>) (See [SQL Server](<Database Driver Reference/SQL Server Database>) for an example), or if password is provided through another means (such as [aws secrets](https://documentation.red-gate.com/flyway/flyway-concepts/secrets-management)).

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog.

### Command-line

```powershell
./flyway -password=mysecretpassword info
```

To configure a named environment via command line when using a TOML configuration, prefix `password` with `environments.{environment name}.` for example:

```powershell
./flyway -environments.sample.password=mysecretpassword info
```

### TOML Configuration File

```toml
[environments.default]
password = "mysecretpassword"
```

### Configuration File

```properties
flyway.password=mysecretpassword
```

### Environment Variable

```properties
FLYWAY_PASSWORD=mysecretpassword
```

### API

When using the Java API, you configure your JDBC URL, User and Password via a datasource.

```java
Flyway.configure()
    .datasource("jdbc:h2:mem:flyway_db", "myuser", "mysecretpassword")   
    .load()
```

### Gradle

```groovy
flyway {
    password = 'mysecretpassword'
}
```

### Maven

```xml
<configuration>
    <password>mysecretpassword</password>
</configuration>
```
