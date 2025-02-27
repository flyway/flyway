---
subtitle: Docker Provisioner
---

- **Status:** Preview

{% include enterprise.html %}

This [provisioner](https://documentation.red-gate.com/flyway/flyway-concepts/environments/provisioning) allows for the provisioning and re-provisioning of databases using Docker, specifically leveraging the Docker compose functionality

Prerequisites:
* Either create or use an existing Docker compose file, which defines one or more services which will provision a database

To configure this provisioner:
1. Set the value of the [provisioner parameter](<Configuration/Environments Namespace/Environment Provisioner Setting>) to `docker`
2. Populate the following resolver properties:
    - `composeFile` - (Required)  The Docker compose file to use
    - `services` - (Required) The relevant services exposed by the Docker compose file 
    - `waitTimeout` - (Required) The Docker wait timeout. This takes the form of a number optionally followed by a time unit, `s`, `m`, `h`, or `d`. If no time unit is specified, seconds are assumed.

## Example
This can be used in the TOML configuration like this:
```toml
[environments.development]
url = "jdbc:sqlserver://localhost:1433;databaseName=MyDatabase;trustServerCertificate=true"
user = "MyUser"
password = "${localSecret.MyPasswordKey}"
provisioner = "docker"

[environments.development.resolvers.docker]
composeFile = "compose.yml"
services = [ "development" ]
waitTimeout = "1m"
```

This example is referencing a docker compose file which might look something like this (note that this example is not production ready - it is functional but has plain text passwords):
```yaml
services:
  development:
    build: .
    environment:
      - MSSQL_PASSWORD=MyPassword
      - ACCEPT_EULA=Y
    ports:
      - "1433:1433"
    healthcheck:
      test: [ "CMD", "/opt/mssql-tools/bin/sqlcmd", "-U", "MyUser", "-P", "MyPassword", "-d", "MyDatabase", "-Q", "SELECT 1"]
      interval: 10s
      retries: 20
```

The build step is here being controlled by a Dockerfile:
```dockerfile
FROM mcr.microsoft.com/mssql/server:2022-latest

COPY entrypoint.sh entrypoint.sh

ENTRYPOINT [ "/bin/bash", "entrypoint.sh" ]
```

The bash script in this example (note that this example is not production ready - it is functional but not particularly robust):
```shell
#!/bin/bash

/opt/mssql/bin/sqlservr &
for _ in {1..25}; do /opt/mssql-tools/bin/sqlcmd -S localhost -U MyUser -P "$MSSQL_PASSWORD" -q "CREATE DATABASE MyDatabase" && break || (echo "DB not up yet ..." && sleep 15); done

while true; do sleep 1000; done
```
