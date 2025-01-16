---
subtitle: Command-line parameters
---

Flyway commands can be executed either via `> flyway [options] [command]` or `> flyway [command] [options]`. However, note that [namespace short-circuiting](https://documentation.red-gate.com/display/FD/Configuration+namespaces) is only available when the command is specified before the options.

For parameters which can be set on each command, see individual [commands](<Commands>).
All settings in the [Flyway namespace](<Configuration/Flyway Namespace>) can also be set as commandline parameters.

## Universal parameters

The following flags provide helpful information without carrying out any other operations:

| Flag                       | Purpose                                          |
|----------------------------|--------------------------------------------------|
| `--help`<br/>`-h`<br/>`-?` | Print the list of available commands and options |
| `--version`<br/>`-v`       | Print the Flyway version                         |

The following options modify the console output:

| Flag                                                           | Purpose                                                         |
|----------------------------------------------------------------|-----------------------------------------------------------------|
| `-X`                                                           | Extended debug output enabled                                   |
| `-q`                                                           | Quiet mode, suppress all output, except for errors and warnings |
| [-color](<Command-line Parameters/Color Parameter>)            | Colorize the terminal output                                    |
| [-outputType](<Command-line Parameters/Output Type Parameter>) | Human or machine-readable output                                |

The following command line options modify behavior for all commands:

| Parameter                                                                        | Tier      | Type   | Description                                                         |
|----------------------------------------------------------------------------------|-----------|--------|---------------------------------------------------------------------|
| [`configFileEncoding`](<Command-line Parameters/Config File Encoding Parameter>) | Community | String | The file encoding to use when loading Flyway configuration files.   |
| [`configFiles`](<Command-line Parameters/Config Files Parameter>)                | Community | String | The Flyway configuration files to load.                             |
| [`workingDirectory`](<Command-line Parameters/Working Directory Parameter>)      | Community | String | The working directory to consider when dealing with relative paths. |

The following settings can be overridden for all commands:

| Setting                                                              | Tier      | Type         | Description                                                   |
|----------------------------------------------------------------------|-----------|--------------|---------------------------------------------------------------|
| [`loggers`](<Configuration/Flyway Namespace/Flyway Loggers Setting>) | Community | String array | Loggers to use.                                               |
| [`email`](<Configuration/Flyway Namespace/Flyway Email Setting>)     | Community | String       | Email to be used in conjunction with a personal access token. |
| [`token`](<Configuration/Flyway Namespace/Flyway Token Setting>)     | Community | String       | Personal access token used for licensing Flyway.              |

The following deprecated settings can be overridden for all commands:

| Setting                                                                     | Tier  | Type   | Description              |
|-----------------------------------------------------------------------------|-------|--------|--------------------------|
| [`licenseKey`](<Configuration/Flyway Namespace/Flyway License Key Setting>) | Teams | String | Your Flyway license key. |

## Configuration from Standard Input

You can provide configuration options to the standard input of the Flyway command line, using the [`configFiles`](<Command-line Parameters/Config Files Parameter>)  option. Flyway will expect such configuration to be in the same format as a configuration file.

This allows you to compose Flyway with other operations. For instance, you can decrypt a config file containing login credentials and pipe it straight into Flyway.
