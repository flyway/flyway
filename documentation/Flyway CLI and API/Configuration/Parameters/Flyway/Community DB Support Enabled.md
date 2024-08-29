---
pill: communityDBSupportEnabled
subtitle: flyway.communityDBSupportEnabled
redirect_from: Configuration/communityDBSupportEnabled/
---

# Community DB Support Enabled

## Description
Whether to enable or disable support for community databases. Set to `false` to prevent Flyway from using a community database connection.
This is especially useful for production environments where using a community database connection could be undesirable.

## Default
true

## Usage

### Commandline
```powershell
./flyway -communityDBSupportEnabled="false" migrate
```

### TOML Configuration File
```toml
[flyway]
communityDBSupportEnabled = false
```

### Configuration File
```properties
flyway.communityDBSupportEnabled=false
```

### Environment Variable
```properties
FLYWAY_COMMUNITY_DB_SUPPORT_DISABLED=false
```

### API
```java
Flyway.configure()
    .communityDBSupportEnabled(false)
    .load()
```

### Gradle
```groovy
flyway {
    communityDBSupportEnabled = false
}
```

### Maven
```xml
<configuration>
    <communityDBSupportEnabled>false</communityDBSupportEnabled>
</configuration>
```
