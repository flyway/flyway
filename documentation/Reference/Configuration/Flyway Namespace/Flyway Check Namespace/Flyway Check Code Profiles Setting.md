---
subtitle: flyway.check.code.profiles
---

{% include enterprise.html %}

## Description

A table defining the code review profiles available to this project, keyed by profile name. Profiles are defined
here and named per invocation on the command line. Each entry lists the
rules and rule groups that [`profile`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Profile Setting>) excludes when that profile is named.

Both individual rule codes (`RG06`) and SQLFluff rule groups (`layout`) are accepted. Excluding a rule that the
installed SQLFluff does not know about is not an error - SQLFluff writes a warning and carries on.

Flyway ships with a built-in `schemaModel` profile. Defining a profile of the same name here replaces it outright, so
list every rule you want excluded rather than only the ones you want to add. Any other name defines a new profile.

Profile names are matched case-insensitively.

## Type

Table of string arrays

## Default

<i>empty - no project-defined profiles</i>

The built-in `schemaModel` profile is always available and is not an entry in this table, so an empty table does not
mean there are no profiles to name.

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### TOML Configuration File

```toml
[flyway.check.code.profiles]
schemaModel = ["capitalisation", "layout", "RG06", "RG09"]
preCommit = ["layout"]
```

Naming either of these with `check.code.profile` excludes exactly the rules listed.
