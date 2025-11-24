---
subtitle: flyway.check.scriptFilename
---

{% include commandlineonly.html %}

## Description

Specify an individual script to run code analysis on. Only used if [scope](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Scope Setting>) is 'SCRIPT'.
This will be resolved relative to the [working directory](<Command-line Parameters/Working Directory Parameter>).

## Type

String

## Default

This will default to the value of [prepare.scriptFilename](<Configuration/Flyway Namespace/Flyway Prepare Namespace/Flyway Prepare Script Filename Setting>).

## Usage

### Command-line

```powershell
./flyway check -code -scriptFilename=custom_script.sql
```

### TOML Configuration File

```toml
[flyway.check]
scriptFilename = "custom_script.sql"
```
