---
subtitle: flyway.tags.definitions
---

- **Status:** Preview

{% include enterprise.html %}

## Description

A table that links versioned migrations to the tags for that migration.
Migrations that do not have tag definitions specified will automatically gain the tag `untagged`.

Tag definitions may be used with tag conditions such as `anyOf` to select or order a sequence of migrations.
By default, if a migration version is listed in the tag definitions table then this migration must exist if tag
conditions are used for selection or ordering. This behavior can be changed using the [`failOnMissing`](<Configuration/Flyway Namespace/Flyway Tags Namespace/Flyway Tags Fail On Missing Setting>) setting.

Example:

```toml
[flyway.tags.definitions]
"1.0.0" = ["tagA", "tagB"]
"1.0.1" = ["tagA", "tagC"]
```

In this configuration, there are two scripts with two tags each.
Additional migration scripts - for example `1.0.3` - would only have the tag `untagged`.

Repeatable migrations are not supported with tags at this time.

## Type

Table of string arrays

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### TOML Configuration File

A versioned migration can be associated with one or more tags by adding it to the `flyway.tags.definitions` table. For
example:

```toml
[flyway.tags.definitions]
"1.0.0" = ["tagA", "tagB"]
"1.0.1" = ["tagA", "tagC"]
"1.0.2" = ["tagC"]
```


