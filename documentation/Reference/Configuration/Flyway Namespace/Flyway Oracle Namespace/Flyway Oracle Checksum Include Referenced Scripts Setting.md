---
subtitle: flyway.oracle.checksumIncludeReferencedScripts
---

{% include teams.html %}

## Description

Flyway tracks the checksums of applied migrations to detect whether any changes have been made to migration files after they were applied.

This parameter specifies whether to include referenced scripts when calculating migration checksums. When SQL*Plus mode is enabled, migrations can reference external files via `@` statements, and SQL*Plus login scripts (`login.sql`, `glogin.sql`) are also loaded automatically. By default, the contents of all these referenced scripts are included in the checksum calculation.

Setting this to `false` causes Flyway to calculate checksums based only on the main migration script content, excluding any referenced files.

_note:_
- This setting only applies when SQL*Plus mode (`flyway.oracle.sqlplus`) is enabled. Without SQL*Plus mode, there are no referenced scripts to include.
- This parameter has no effect in Native Connectors mode, where referenced scripts are executed directly by the underlying native tooling rather than parsed by Flyway.
- It is generally advisable to configure this setting once at the start of a new deployment and leave it unchanged. Changing it on an existing deployment will alter how checksums are calculated, which may cause the [validate](<Commands/validate>) command to fail for previously applied migrations. If you do need to change this setting, run [repair](<Commands/Repair>) command afterwards to realign the stored checksums.

## Type

Boolean

## Default

`true`

## Usage

### Command-line

```powershell
./flyway -oracle.checksumIncludeReferencedScripts="false" info
```

### TOML Configuration File

```toml
[flyway.oracle]
checksumIncludeReferencedScripts = false
```

### Configuration File

```properties
flyway.oracle.checksumIncludeReferencedScripts=false
```

### Environment Variable

```properties
FLYWAY_ORACLE_CHECKSUM_INCLUDE_REFERENCED_SCRIPTS=false
```

### API

```java
OracleConfigurationExtension oracleConfigurationExtension = configuration.getPluginRegister().getExact(OracleConfigurationExtension.class);
oracleConfigurationExtension.setChecksumIncludeReferencedScripts(false);
```
