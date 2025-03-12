---
pill: token
subtitle: flyway.token
redirect_from: Configuration/token/
---

## Description

A personal access token (PAT) to be used in conjunction with the [`email`](<Configuration/Flyway Namespace/Flyway Email Setting>)
configuration parameter. This is used to license Flyway to access Teams or Enterprise features.

You should treat the token like a password and it should **not** be stored directly in your code repository. Use of a secrets manager or environment variable are much safer solutions.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

Not needed in conjunction with Flyway Desktop as Flyway Desktop will handle auth.

### Command-line

```bash
./flyway -email="foo.bar@red-gate.com" -token="1234ABCD"
```

### TOML

```properties
[flyway]
email = "foo.bar@red-gate.com"
token = "1234ABCD"
```

### Environment Variable

```properties
FLYWAY_EMAIL=foo.bar@red-gate.com
FLYWAY_TOKEN=1234ABCD
```

### Maven

```xml
<configuration>
  <pluginConfiguration>
    <email>foo.bar@red-gate.com</email>
    <token>1234ABCD</token>
  </pluginConfiguration>
</configuration>
```

### API

```java
Flyway flyway = Flyway.configure().load();
flyway.getConfigurationExtension(PATTokenConfigurationExtension.class)
        .setEmail("foo.bar@red-gate.com");
flyway.getConfigurationExtension(PATTokenConfigurationExtension.class)
        .setToken("1234ABCD");  
```
