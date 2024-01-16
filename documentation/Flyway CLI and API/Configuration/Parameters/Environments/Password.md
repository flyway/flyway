---
pill: password
subtitle: flyway.password
redirect_from: Configuration/password/
---

# Password

## Description
The password to use to connect to the database

This can be omitted if the password is baked into the [url](Configuration/parameters/environments/url) (See [Sql Server](Supported Databases/SQL Server#windows-authentication) for an example), or if password is provided through another means (such as [aws secrets](Configuration/Secrets Management)).

## Usage

### Commandline
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
