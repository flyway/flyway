---
subtitle: testConnection
---

## Description
The `testConnection` command attempts to establish a connection to the database using the configured [environment](<Configuration/Flyway Namespace/Flyway Environment Setting>). 
This command is designed to verify that Flyway and other engines used by Flyway can reach your database, ensuring the connectivity and configuration are correct before running migrations or other operations.

Currently supported engines:
- Flyway Engine
- Comparison Engines

If no environment is specified, the default environment will be used, if it exists.

- On success: Displays 'Connection successful' for each successful engine connected and exits with a status code 0.
- On failure: Displays database specific connection error with diagnostic information and a non-zero status code.
- On success with JSON output: Displays a message that lists the engines that Flyway connected to

This command only returns output and does not make any changes to your database; unlike [info](<Commands/Info>) which may also trigger callbacks, migration validation or checksum evaluation.

### Usage Example

#### Test connection to the development environment via command line
```sh
flyway testConnection -environment=development
```


## Parameters

- All standard Flyway configuration options that affect connection (e.g., `-url`, `-user`, `-password`, or environment selection like `-environment={name}`) are supported.


_For a full list of configuration options, see [Flyway configuration](https://documentation.red-gate.com/flyway/reference/configuration/)._


## JSON output format

```json
{
  "successfulConnections" : "Flyway"
}
```
