---
pill: user
subtitle: flyway.user
redirect_from: Configuration/user/
---

# User

## Description
The user to use to connect to the database.

This can be omitted if the user is baked into the [url](Configuration/parameters/url) (See [Sql Server](Supported Databases/SQL Server#windows-authentication) for an example).

## Usage

### Commandline
```powershell
./flyway -user=myuser info
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
```java
Flyway.configure()
    .user("myuser")
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
