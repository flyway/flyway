---
subtitle: Environment Variable
---

The environment variable resolver allows you to inject environment variables into your TOML configuration using the syntax `${env.VARIABLE_NAME}`. 
This works consistently across all Flyway namespace parameters and environment properties in TOML configuration.

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honored.

### TOML Configuration File

```toml
[environments.mydevdb]
user = "${env.DATABASE_USERNAME}"
password = "${env.DATABASE_PASSWORD}"

[flyway]
installedBy = "${env.CI_PIPELINE_NAME}"
defaultSchema = "${env.TARGET_SCHEMA}"
locations = ["${env.MIGRATION_DIR}"]

[flyway]
email = "${env.FLYWAY_EMAIL}"
token = "${env.FLYWAY_TOKEN}"
```

