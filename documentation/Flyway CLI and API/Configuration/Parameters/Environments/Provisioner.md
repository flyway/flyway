---
subtitle: Provisioner
---

# Provisioner
{% include enterprise.html %}

## Description
The name of the provisioner to use for a database connection. For a full list of provisioners see [here](Configuration/Provisioners).

## Usage

### Commandline
The provisioner property cannot currently be set over the command line

### TOML Configuration File
```toml
[environments.development]
url = "${clone.url}databaseName=my-database"
provisioner = "clone"

[environments.development.resolvers.clone]
url = "https://clone.red-gate.com:1234/cloning-api"
dataImage = "mssql-empty"
dataContainer = "MyContainer"
dataContainerLifetime = "1h"
authenticationToken = "${localSecret.RedgateCloneToken}"
```

### Configuration File
The provisioner property cannot be set using a .conf configuration file

### Environment Variable
The provisioner property does not have a dedicated environment variable

### API
The provisioner property cannot currently be set using the API

### Gradle
The provisioner property cannot currently be set using Gradle

### Maven
The provisioner property cannot currently be set using Maven
