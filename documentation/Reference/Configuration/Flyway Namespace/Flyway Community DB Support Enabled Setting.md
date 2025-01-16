---
pill: communityDBSupportEnabled
subtitle: flyway.communityDBSupportEnabled
redirect_from: Configuration/communityDBSupportEnabled/
---

## Description

Whether to enable support for community databases. Set to
`false` to prevent Flyway from using a community database connection.
This is especially useful for production environments where using a community database connection could be undesirable.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be configured via Flyway Desktop, although it will be honoured.

### Command-line

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
