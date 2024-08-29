---
pill: token
subtitle: flyway.token
redirect_from: Configuration/token/
---

# Token

## Description

A personal access token (PAT) to be used in conjunction with the [`email`](configuration/parameters/flyway/email)
configuration parameter. Used to authenticate Flyway to use either Teams or Enterprise.

## Usage

### Commandline
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
