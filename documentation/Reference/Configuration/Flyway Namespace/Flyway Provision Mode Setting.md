---
subtitle: flyway.provisionMode
---

## Description

Controls how flyway runs the [Provisioner](<Configuration/Environments Namespace/Environment Provisioner Setting>) on the [target environment](<Configuration/Flyway Namespace/Flyway Environment Setting>), if one was configured.

### Provision

Default behavior. When `-provisionMode=provision` is set, Flyway will attempt to `provision` the environment if it does not exist. For example, if an environment used the [`create-database`](<Configuration/Environments Namespace/Environment Provisioner Setting/Create Database Provisioner>) provisioner, Flyway would attempt to create the database if it did not already exist before continuing.

### Re-provision

When `-provisionMode=reprovision` is set, Flyway will attempt to reset the target environment to a state as if it were newly provisioned. **This is a destructive operation**. Flyway will raise an error if `reprovision` is attempted against an environment without a provisioner defined or where `reprovision` is not possible.

### Skip

When `-provisionMode=skip` is set, flyway should not attempt to provision or re-prosivion the environment.

## Type

String

## Default

`provision`

## Usage

### Flyway Desktop

This property can't be configured manually from Flyway Desktop.

### Command-line

Request flyway to reset a temporary development environment using `reprovision`:
```powershell
./flyway -environment=tempDev -provisionMode=reprovision info
```

### TOML Configuration File

It is not recommended to set this property using the configuration file. 

This option is intended as an on-demand configuration for specific individual operations. Setting `provisionMode` to `reprovision` in the configuration file means this is set for all Flyway operations by default. This may lead to accidentally running re-provision against unintended targets, which could potentially cause data loss.