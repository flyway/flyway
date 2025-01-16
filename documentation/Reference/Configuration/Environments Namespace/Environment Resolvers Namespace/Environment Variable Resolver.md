---
subtitle: Environment Variable
---

For parameters in the [environment namespace](Configuration/Environments Namespace) it is possible to inject variables into the TOML using the environment variable resolver.

This expects an entry of the form `${env.VARIABLE_NAME}`.

_Note: Flyway parameters outside of the environment namespace have their own configuration - see the 'Environment Variable' section of the parameter of interest_

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop, although it will be honoured.

### TOML Configuration File

```toml
[environments.mydevdb]
user = "${env.DATABASE_USERNAME}"
```

