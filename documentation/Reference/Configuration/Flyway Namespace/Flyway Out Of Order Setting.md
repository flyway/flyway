---
pill: outOfOrder
subtitle: flyway.outOfOrder
redirect_from: Configuration/outOfOrder/
---

## Description

Allows migrations to be run "out of order".

If you already have versions `1.0` and `3.0` applied, and now a version
`2.0` is found, it will be applied too instead of being ignored.

_Note_:

- _This parameter has no impact on Undo operation._

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured, and it can be configured as an advanced parameter in operations on the Migrations page.

### Command-line

```powershell
./flyway -outOfOrder="true" info
```

### TOML Configuration File

```toml
[flyway]
outOfOrder = true
```

### Configuration File

```properties
flyway.outOfOrder=true
```

### Environment Variable

```properties
FLYWAY_OUT_OF_ORDER=true
```

### API

```java
Flyway.configure()
    .outOfOrder(true)
    .load()
```

### Gradle

```groovy
flyway {
    outOfOrder = true
}
```

### Maven

```xml
<configuration>
    <outOfOrder>true</outOfOrder>
</configuration>
```
