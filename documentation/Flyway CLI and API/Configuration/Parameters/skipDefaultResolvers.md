---
pill: skipDefaultResolvers
subtitle: flyway.skipDefaultResolvers
redirect_from: Configuration/skipDefaultResolvers/
---

# skipDefaultResolvers

## Description
Whether default built-in resolvers (sql and jdbc) should be skipped. If `true`, only [custom resolvers](Configuration/Parameters/Resolver) are used.

## Default
false

## Usage

### Commandline
```powershell
./flyway -skipDefaultResolvers="true" info
```

### Configuration File
```properties
flyway.skipDefaultResolvers=true
```

### Environment Variable
```properties
FLYWAY_SKIP_DEFAULT_RESOLVERS=true
```

### API
```java
Flyway.configure()
    .skipDefaultResolvers(true)
    .load()
```

### Gradle
```groovy
flyway {
    skipDefaultResolvers = true
}
```

### Maven
```xml
<configuration>
    <skipDefaultResolvers>true</skipDefaultResolvers>
</configuration>
```
