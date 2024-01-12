---
pill: CLI configuration order
subtitle: overriding order
---

Settings are loaded in the following order (higher items in the list take precedence over lower ones):
1. Command-line arguments
1. Environment variables
1. Standard input
1. [Custom config files](Configuration/Configuration Files) <br>TOML Configuration file (<filename>.toml) takes precedence over a legacy configuration (<filename>.conf) file and then the following filesystem locations
   1. `<current-dir>/`
   1. `<user-home>/`
   1. `<install-dir>/conf/`
1. Flyway command-line defaults

This means that if `flyway.url` is both present in a config file and passed as `-url=` from the command-line,
the command-line argument will take precedence and be used.

If you are unsure about where Flyway is taking its configuration from then adding `-X` (capital `X`) to the command line will enable extended debugging.
`./flyway info -X`