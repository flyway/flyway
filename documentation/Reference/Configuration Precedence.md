---
subtitle: overriding order
---

## Configuration setting precedence

Settings are loaded in the following order (higher items in the list take precedence over lower ones):

1. Command-line arguments
1. Environment variables
1. Standard input
1. Configuration files
1. Flyway command-line defaults

This means that if `flyway.url` is both present in a config file and passed as `-url=` from the command-line,
the command-line argument will take precedence and be used.

## Configuration file location precedence

By default, Flyway will load settings from configuration files located in directories in this order (higher items in the list take precedence over lower ones):

1. {[`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)}/
1. {executionDir}/
1. {userhome}/
1. {installDir}/conf/

Note that this behavior can be overridden by explicitly specifying config files using the [configFiles](<Command-line Parameters/Config Files Parameter>) command line parameter.

## Toml precedence over conf

TOML Configuration files (<filename>.toml) take precedence over legacy configuration (<filename>.conf) files

Flyway does not support the simultaneous use of both Legacy CONF format and TOML format in the same environment.

It will interpret configurations using only one of these methods. To determine which method to use, Flyway adheres to specific precedence rules as below:

* If both a TOML file and a CONF file are specified in the configFiles configuration parameter, Flyway will generate an error.
* If a TOML file is specified via -configFiles, Flyway will operate in modern configuration mode. This means it will ignore all CONF files configured via the Environment Variable FLYWAY_CONFIG_FILES or present in the file system.
* If a CONF file is specified via -configFiles, Flyway will operate in legacy configuration mode. In this case, it will ignore all TOML files configured via the Environment Variable FLYWAY_CONFIG_FILES or present in the file system.
* If a TOML file is specified via the Environment Variable FLYWAY_CONFIG_FILES, Flyway will operate in modern configuration mode. This means it will ignore all CONF files present in the file system.
* If a CONF file is specified via the Environment Variable FLYWAY_CONFIG_FILES, Flyway will operate in legacy configuration mode. In this case, it will ignore all TOML files present in the file system.
* If the Config Files configuration parameter is not provided via either -configFiles or the Environment Variable FLYWAY_CONFIG_FILES, Flyway will search for configuration files in the local file system, specifically in locations mentioned earlier in this documentation.
* If Flyway finds any TOML file in the designated local file system locations, it will use modern configuration mode and ignore all CONF files.
* If Flyway finds any CONF file in the designated local file system locations, it will use legacy configuration mode.
* If neither a TOML file nor a CONF file is found, Flyway will default to using modern configuration mode.

## Debugging Flyway configuration evaluation

If you are unsure about where Flyway is taking its configuration from then adding `-X` (capital `X`) to the command line will enable extended debugging.
`./flyway info -X`