---
subtitle: flyway.offlinePermit
redirect_from: Configuration/offlinePermit/
---

{% include redgate.html %}

## Description

This specifies the Flyway offline license permit used to authenticate Enterprise features. The value is the permit itself, not a path to a file. Please see the [License permit](https://documentation.red-gate.com/fd/license-permits-224919672.html) page for more information on how to obtain one.
- Offline license permits are used where your Flyway installation cannot connect to the internet to refresh its license.
- When your license expires you will need to create a fresh offline license permit and replace the old value in order to continue using licensed features, otherwise Flyway will revert to the Community feature set (only foundational capabilities).
- Only one of `offlinePermit` or [`offlinePermitPath`](<Configuration/Flyway Namespace/Flyway Offline Permit Path Setting>) can be configured. Flyway will fail with an error if both are set.

You should treat the permit like a password and it should **not** be stored directly in your code repository. Use of an environment variable is a much safer solution. Flyway masks this value when it prints its configuration.

### Permit length limits

Permits are large, often several kilobytes. Some operating systems and shells limit how long a command line or environment variable can be. For example, the Windows command prompt (`cmd.exe`) limits a command line to 8191 characters. If the permit is truncated, Flyway will report it as invalid and include the number of characters it received and the last three characters of the value, so you can compare them with your permit.

If you run into these limits, use [`offlinePermitPath`](<Configuration/Flyway Namespace/Flyway Offline Permit Path Setting>) to read the permit from a file instead.

## Type

String

## Default

<i>none</i>

## Usage

### Flyway Desktop

Not needed in conjunction with Flyway Desktop as Flyway Desktop will handle licensing.

### Command-line

```bash
./flyway -offlinePermit="your_offline_permit"
```

### TOML

```toml
[flyway]
offlinePermit = "your_offline_permit"
```

### Environment Variable

```properties
FLYWAY_OFFLINE_PERMIT=your_offline_permit
```

### Maven

```xml
<configuration>
  <pluginConfiguration>
    <offlinePermit>your_offline_permit</offlinePermit>
  </pluginConfiguration>
</configuration>
```

### API

```java
Flyway flyway = Flyway.configure().load();
flyway.getConfigurationExtension(OfflinePermitConfigurationExtension.class)
        .setOfflinePermit("your_offline_permit");
```
