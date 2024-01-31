---
subtitle: Config Files
redirect_from: /documentation/configfiles/
---

Flyway supports loading configuration via text files.
By default, Flyway will load configuration files from the following locations:

- {installDir}/conf/
- {userhome}/
- {executionDir}/
- {[workingDirectory](Configuration/Parameters/Flyway/Working Directory)}/


Additionally, you can make Flyway load other configurations using the [configFiles](Configuration/Parameters/Flyway/Config Files) configuration parameter.

By default Flyway loads configuration files using UTF-8. 
To use an alternative encoding see [Config File Encoding](Configuration/Parameters/Flyway/Config File Encoding)
## TOML Format

We have introduced a new configuration format, [TOML](Configuration/What is TOML) which is the successor to the traditional conf format. You can find out more at [TOML Configuration](Configuration/TOML Configuration File/).

Flyway will use the configuration file with a .toml extension in preference to a .conf file if both are present.

## Legacy CONF Format 

Config files have the following structure:

```properties
# Settings are simple key-value pairs
flyway.key=value
# Single line comment start with a hash

# Long properties can be split over multiple lines by ending each line with a backslash
flyway.locations=filesystem:my/really/long/path/folder1,\
filesystem:my/really/long/path/folder2,\
filesystem:my/really/long/path/folder3

# These are some example settings
flyway.url=jdbc:mydb://mydatabaseurl
flyway.schemas=schema1,schema2
flyway.placeholders.keyABC=valueXYZ
```

## Reference

See [configuration](Configuration/parameters) for a full list of supported configuration parameters.

Here is a sample TOML configuration file:

```properties
# More information on the parameters can be found here: https://documentation.red-gate.com/flyway/flyway-cli-and-api/configuration/parameters

[environments.sample]
url = "jdbc:h2:mem:db"
user = "sample user"
password = "sample password"
# jarDirs = ["path/to/java/migrations"]
# driver =
# schemas =
# connectRetries =
# connectRetriesInterval =
# initSql =
# jdbcProperties =
# resolvers =

[flyway]
# environment = "sample" # It is recommended to configure environment as a commandline argument. This allows using different environments depending on the caller.
# locations = ["filesystem:path/to/sql/files"]

# [environments.build]
# url = "jdbc:sqlite::memory:"
# user = "buildUser"
# password = "buildPassword"

# [flyway.check]
# buildEnvironment = "build"
```

## Precedence

Flyway does not support the simultaneous use of both Legacy CONF format and TOML format in the same environment. 

It will interpret configurations using only one of these methods. To determine which method to use, Flyway adheres to specific precedence rules as below:

- If both a TOML file and a CONF file are specified in the [configFiles configuration parameter](Configuration/Parameters/Flyway/Config Files), Flyway will generate an error.
- If a TOML file is specified via `-configFiles`, Flyway will operate in modern configuration mode. This means it will ignore all CONF files configured via the Environment Variable `FLYWAY_CONFIG_FILES` or present in the file system.
- If a CONF file is specified via `-configFiles`, Flyway will operate in legacy configuration mode. In this case, it will ignore all TOML files configured via the Environment Variable `FLYWAY_CONFIG_FILES` or present in the file system.
- If a TOML file is specified via the Environment Variable `FLYWAY_CONFIG_FILES`, Flyway will operate in modern configuration mode. This means it will ignore all CONF files present in the file system.
- If a CONF file is specified via the Environment Variable `FLYWAY_CONFIG_FILES`, Flyway will operate in legacy configuration mode. In this case, it will ignore all TOML files present in the file system.
- If the Config Files configuration parameter is not provided via either `-configFiles` or the Environment Variable `FLYWAY_CONFIG_FILES`, Flyway will search for configuration files in the local file system, specifically in locations mentioned earlier in this documentation.
- If Flyway finds any TOML file in the designated local file system locations, it will use modern configuration mode and ignore all CONF files.
- If Flyway finds any CONF file in the designated local file system locations, it will use legacy configuration mode.
- If neither a TOML file nor a CONF file is found, Flyway will default to using modern configuration mode.
