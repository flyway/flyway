---
pill: cleanDisabled
subtitle: flyway.cleanDisabled
redirect_from: Configuration/cleanDisabled/
---

## Description

Whether to disable clean. This is especially useful for production environments where running clean can be a career limiting move. Set to
`false` to allow `clean` to execute.

## Type

Boolean

## Default

`true`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.
This is hard-coded to `true` under the hood when Flyway Desktop reprovisions the shadow database.

### Command-line

```powershell
./flyway -cleanDisabled="false" clean
```

### TOML Configuration File

```toml
[flyway]
cleanDisabled = false
```

### Configuration File

```properties
flyway.cleanDisabled=false
```

### Environment Variable

```properties
FLYWAY_CLEAN_DISABLED=false
```

### API

```java
Flyway.configure()
    .cleanDisabled(false)
    .load()
```

### Gradle

```groovy
flyway {
    cleanDisabled = false
}
```

### Maven

```xml
<configuration>
    <cleanDisabled>false</cleanDisabled>
</configuration>
```
