---
subtitle: flyway.tags.deploymentOrder
---

- **Status:** Preview

{% include enterprise.html %}

## Description

A tag condition containing a sequence of tags to order migrations by.

If a tag is associated with multiple migrations, then those migrations will be selected in version order where that tag 
appears in the order sequence. If a migration is associated with multiple tags in the order sequence, then the migration
will be selected once at the first occurrence in the order sequence. Remaining tags or migrations that are not referenced
specifically by tags in the deployment order will be in version order at the end of the sequence.

Example:
```toml
[flyway.tags.definitions]
"0.1" = ["tagA", "tagC"]
"0.2" = ["tagB"]
"0.3" = ["tagB", "tagC"]
```

Specifying the ordering `-tags.deploymentOrder=tagB,tagC` would select migrations in the order `0.2`, `0.3`, `0.1`.
If there was an additional version `0.4` migration on disk, this would run last at the end of the sequence.

Note that the `deploymentOrder` setting, like `cherryPick`, always describes the forward order of migrations. 
For example, while `flyway migrate -tags.deploymentOrder=tagB,tagC` would deploy `0.2`, `0.3`, `0.1`, running 
`flyway undo -tags.deploymentOrder=tagB,tagC` will undo operate on the reverse order of `0.1`, `0.3`, `0.2`.

## Type

String array

## Default

<i>none</i>

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```powershell
./flyway "-tags.deploymentOrder=tagA,tagB,tagC" info
```

### TOML Configuration File

```toml
[flyway.tags]
deploymentOrder = ["tagA", "tagB", "tagC"]
```

Repeatable migrations are not supported with tags at this time.

