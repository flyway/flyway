---
subtitle: flyway.offlinePermitPath
redirect_from: Configuration/offlinePermitPath/
---

## Description

This specifies the path to the Flyway offline license permit used to authenticate Enterprise features. Please see the [License permit](https://documentation.red-gate.com/fd/license-permits-224919672.html) page for more information on how to obtain one.
- Offline license permits are used where your Flyway installation cannot connect to the internet to refresh it's license.
- When your license expires you will need to create a fresh offline license permit and replace the old one in this location in order to continue using licensed features, otherwise Flyway will revert to the Community feature set (only foundational capabilities).
- This parameter is defined as a path to a file containing the permit. This is because permits are often too large to fit in the command line.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

Not needed in conjunction with Flyway Desktop as Flyway Desktop will handle licensing.

### Command-line

```bash
./flyway -offlinePermitPath="path_to_offline_permit"
```

### TOML

```toml
[flyway]
offlinePermitPath = "path_to_offline_permit"
```

### Environment Variable

```properties
FLYWAY_OFFLINE_PERMIT_PATH=path_to_offline_permit
```

Note: _Some legacy Flyway documentation refers to the environment variable `REDGATE_LICENSING_PERMIT_PATH`, which serves the same purpose as this.
That variable is now deprecated and may be removed in future releases. If you are setting this up now then you should use the new environment variable instead to avoid future problems._


### Maven

```xml
<configuration>
  <pluginConfiguration>
    <offlinePermitPath>path_to_offline_permit</offlinePermitPath>
  </pluginConfiguration>
</configuration>
```

### API

```java
Flyway flyway = Flyway.configure().load();
flyway.getConfigurationExtension(OfflinePermitConfigurationExtension.class)
        .setOfflinePermitPath("path_to_offline_permit");
```