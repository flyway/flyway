---
pill: Environment
subtitle: flyway.environment
---

# Environment

## Description
The name of the environment you wish Flyway to load configuration from. It points at the specific environment config with the same name.

This can be used to switch between multiple environments set in the TOML config file.

For more on the parameters that can be set inside the environment, see [here](configuration/TOML Configuration File).

## Default
default

## Usage

### Commandline
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
FLYWAY_ENVIRONMENT=env1
```

### API

```java
Flyway.configure()
    .environment("env1")   
    .load()
```
