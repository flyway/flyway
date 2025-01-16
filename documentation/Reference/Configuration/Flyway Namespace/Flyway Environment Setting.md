---
pill: Environment
subtitle: flyway.environment
---

## Description

The name of the environment you wish Flyway to load configuration from. It points at the specific environment config with the same name.

This can be used to switch between multiple environments set in the TOML config file.

For more on the parameters that can be set inside the environment, see [here](<Configuration/Environments Namespace>).

## Type

String

## Default

`"default"`

## Usage

### Flyway Desktop

This can't be set in a config file via Flyway Desktop. This is set explicitly under the hood in Flyway Desktop invocations to flyway command line.

### Command-line

```powershell
./flyway -environment=env1 info
```

### TOML Configuration File

```toml
[environments.env1]
url = "jdbc:h2:mem:flyway_db"
user = "myuser"
password = "mysecretpassword"

[flyway]
environment = "env1"
```

### Environment Variable

```properties
flyway_environment=env1
```

_Note: The environment variable has to be lower case_

### API

```java
Flyway.configure()
    .environment("env1")   
    .load()
```
