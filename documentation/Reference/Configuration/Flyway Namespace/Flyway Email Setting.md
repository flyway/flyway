---
subtitle: flyway.email
redirect_from: Configuration/email/
---

## Description

A Redgate email to be used in conjunction with the [`token`](<Configuration/Flyway Namespace/Flyway Token Setting>) parameter to
configure a personal access token (PAT). Used to authenticate Flyway to use either Teams or Enterprise.

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

```toml
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