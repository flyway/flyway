---
subtitle: flyway.prepare.scriptFilename
---

## Description

The path to the script that will be deployed.

## Type

String

## Default

`deployments/D__deployment.sql`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway prepare -scriptFilename="output.sql"
```

### TOML Configuration File

```toml
[flyway.prepare]
scriptFilename = "output.sql"
```
