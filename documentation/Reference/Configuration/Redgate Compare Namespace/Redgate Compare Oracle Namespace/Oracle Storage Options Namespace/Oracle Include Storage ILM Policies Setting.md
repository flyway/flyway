---
subtitle: redgateCompare.oracle.options.storage.includeStorageIlmPolicies
---

## Description

Include Information Lifecycle Management policies.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can be set from the comparison options settings in Oracle projects.

### Command-line

```powershell
./flyway diff -redgateCompare.oracle.options.storage.includeStorageIlmPolicies=true
```

### TOML Configuration File

```toml
[redgateCompare.oracle.options.storage]
includeStorageIlmPolicies = true
```
