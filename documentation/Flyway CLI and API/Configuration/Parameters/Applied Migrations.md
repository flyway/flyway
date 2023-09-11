---
pill: check.appliedMigrations
subtitle: flyway.check.appliedMigrations
---
# Check: Applied Migrations

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

A comma-separated list of migration ids (migration versions or repeatable descriptions) to apply to create snapshots (generated via [`info`](Commands/info))
See [Check Concept](Concepts/Check Concept) for more information on how to configure the change reports

## Default

None

## Usage

### Commandline
```powershell
./flyway check -changes -check.appliedMigrations="1,2,3"
```

### Configuration File
```properties
flyway.check.appliedMigrations=1,2,3
```