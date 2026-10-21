---
subtitle: flyway.check.code.profiles
---

{% include enterprise.html %}

## Description

A table defining the code review profiles available to this project, keyed by profile name. Profiles are defined
here and named per invocation on the command line. Each entry lists the
rules and rule groups that [`profile`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Profile Setting>) excludes when that profile is named.

Both individual rule codes and SQLFluff rule groups are accepted. A rule code may name either a SQLFluff rule
(`RG06`) or a [regex rule](<Code Review Rules/Configuring Regex Rules>) (`RX001`); regex rule codes are matched
case-insensitively and must be written in full. Rule group excludes and wildcards such as `LT*` only apply to SQLFluff.

One profile can name both SQLFluff and regex rules, as each entry excludes whichever rules it names. An entry that
matches no rule at all is not an error - when SQLFluff is enabled, Flyway warns about it and the run carries on. The
warning never fails the run, even with
[`check.code.failOnError`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Fail On Error Setting>)
set.

The `RG` prefix, and the prefixes SQLFluff uses for its own rules, are
[reserved](<Code Review Rules/Configuring Custom SQLFluff Rules>) for the rules Flyway ships, so don't give your own
rules a code in that space. If you do, an entry naming that code excludes every rule that uses it.

Flyway doesn't ship any profiles, so every profile you name must be defined here.

Profile names are matched case-insensitively.

## Type

Table of string arrays

## Default

<i>empty - no profiles are defined</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### TOML Configuration File

```toml
[flyway.check.code.profiles]
schemaModel = ["capitalisation", "layout", "RG01", "RG03", "RG05", "RG06", "RG09", "RG10", "RG13", "RG14", "RG16", "RG22"]
preCommit = ["layout", "RX001"]
```

Naming either of these with `check.code.profile` excludes exactly the rules listed.

The `schemaModel` profile above is a starting point for analyzing a schema model. It excludes the rules that cannot
fire, or that only produce noise, on the schema model files written by the comparison engine: the DML rules, the script
shape rules, the data-loss DDL rules (a drop in a schema model is a deleted file, not a `DROP` statement), the
`ALTER`-only rules, and the formatting families. Flyway doesn't keep this list up to date as rules are added or
changed, so review it against the rules you run.
