---
pill: email
subtitle: flyway.email
redirect_from: Configuration/email/
---

# Email

## Description

A Redgate email to be used in conjunction with the [`token`](configuration/parameters/flyway/token) parameter to
configure a personal access token (PAT). Used to authenticate Flyway to use either Teams or Enterprise.

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
