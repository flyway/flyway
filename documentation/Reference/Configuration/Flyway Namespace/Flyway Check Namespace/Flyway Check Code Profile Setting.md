---
subtitle: flyway.check.code.profile
---

{% include enterprise.html %}

{% include commandlineonly.html %}

## Description

The name of the profile to run code review with. A profile is a named list of rules and rule groups that the run
excludes, so naming one can only ever narrow the analysis - it never enables a rule your
[rules configuration](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Rules Config Setting>) has turned off.

A profile can name SQLFluff rules and rule groups, and the codes of your
[regex rules](<Code Review Rules/Configuring Regex Rules>). Excluding a regex rule this way affects only the run that
names the profile, unlike setting a rule's `severity` to `disabled`, which turns it off for every run.

If this parameter is not set, code review behaves exactly as it always has: every enabled rule is applied. There is
no profile meaning "everything"; that is simply the default.

A profile is a property of the run rather than of the project, so it is named per invocation. Flyway Desktop names one
when it analyzes a schema model, while a pipeline analyzing migrations names none - and both may be pointed at the same
project. Giving a project a default profile is not supported.

Profiles are defined in [`profiles`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Profiles Setting>).
Flyway doesn't ship any, so a profile must be defined there before it can be named.

Naming a profile that isn't defined in your configuration fails the command and lists the profiles that are available.

Flyway cannot select a profile for you. Flyway Desktop copies the SQL to a temporary file and runs with
`check.scope=script`, so the paths never resemble the schema model; the MCP server points `check.scriptFilename` at
the file where it lives. Either way, the profile has to be named explicitly.

Reports do not record which profile ran; a profiled run simply reports fewer violations.

Note that if SQLFluff discovers a configuration file for itself - which can only happen when neither
`check.rulesConfig` nor the bundled `conf/sqlfluff.cfg` resolves - the profile exclusions replace, rather than add to,
any `exclude_rules` in that file.

## Type

String

## Default

<i>none - every enabled rule runs</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop. It is set explicitly under the hood in Flyway Desktop
invocations of the Flyway command line.

### Command-line

```powershell
./flyway check -code "-check.code.profile=schemaModel"
```

This assumes a `schemaModel` profile is defined in
[`profiles`](<Configuration/Flyway Namespace/Flyway Check Namespace/Flyway Check Code Profiles Setting>), as in its
example.

