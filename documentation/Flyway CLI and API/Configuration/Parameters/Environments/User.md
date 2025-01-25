---
pill: user
subtitle: flyway.user
redirect_from: Configuration/user/
---

# User

## Description
The user to use to connect to the database.

This can be omitted if the user is baked into the [url](Configuration/parameters/environments/url) (See [SQL Server](<Supported Databases/SQL Server Database>) for an example).

## Usage

### Commandline
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
