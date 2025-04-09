---
subtitle: flyway.user
redirect_from: Configuration/user/
---

## Description

The user to use to connect to the database.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can be set from the connection dialog.

### Command-line

```powershell
./flyway -user=myuser info
```

To configure a named environment via command line when using a TOML configuration, prefix `user` with `environments.{environment name}.` for example:

```powershell
./flyway -environments.sample.user=myuser info
```

### TOML Configuration File

```toml
[environments.default]
user = "myuser"
```

### Configuration File

```properties
flyway.user=myuser
```

### Environment Variable

```properties
FLYWAY_USER=myuser
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
    user = 'myuser'
}
```

### Maven

```xml
<configuration>
    <user>myuser</user>
</configuration>
```
