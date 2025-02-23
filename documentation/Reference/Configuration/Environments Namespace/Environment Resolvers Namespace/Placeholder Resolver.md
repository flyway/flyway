---
subtitle: Placeholder Resolver
---

This is a [property resolver](https://documentation.red-gate.com/flyway/flyway-concepts/environments/resolvers) which
can be used to derive values based upon user defined placeholder values.

This resolver expects an entry of the form `${placeholder.PLACEHOLDER_NAME}` where `PLACEHOLDER_NAME` is defined under
the `[flyway.placeholders]` namespace.

Note: Only user defined placeholders are supported, default `${flyway:property}` style placeholders are not supported.

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[flyway.placeholders]
backupFilePrefix = '\\FILE-SERVER\SQLServer\Backups'

[environments.shadow.resolvers.backup]
backupFilePath = '${placeholder.backupFilePrefix}\Backup.bak'
```

