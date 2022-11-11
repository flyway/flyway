---
layout: documentation
menu: configuration
pill: password
subtitle: flyway.password
redirect_from: /documentation/configuration/password/
---

# Password

## Description
The password to use to connect to the database

This can be omitted if the password is baked into the [url](/documentation/configuration/parameters/url) (See [Sql Server](/documentation/database/sqlserver#windows-authentication) for an example), or if password is provided through another means (such as [aws secrets](/documentation/configuration/awsSecretsManager)).

## Usage

### Commandline
```powershell
./flyway -password=mysecretpassword info
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
```java
Flyway.configure()
    .password("mysecretpassword")
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