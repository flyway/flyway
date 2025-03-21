---
subtitle: flyway.tags
---

- **Status:** Preview

{% include enterprise.html %}

This namespace contains the configuration options for migration tags.

<div id="children">
{% include childPages.html %}
</div>

## Description

Migration scripts can be assigned one or more custom tags. Tags provides a convenient way to [`cherryPick`](<Configuration/Flyway Namespace/Flyway Cherry Pick Setting>)
and order groups of scripts together when used with other flyway commands such as `info`, `migrate` and `undo` through
the use of tag conditions.

When using tag conditions to select which migration scripts are deployed or the order of the deployment, [`outOfOrder`](<Configuration/Flyway Namespace/Flyway Out Of Order Setting>)
must be enabled.

If you prefer to select or control the order of individual migrations rather than sets of migrations, you can use [`cherryPick`](<Configuration/Flyway Namespace/Flyway Cherry Pick Setting>)
directly. Tag conditions and `cherryPick` cannot be used simultaneously.

If you want to run migrations up to a specific target version, you can use the [`target`](<Configuration/Flyway Namespace/Flyway Target Setting>)
setting. Tag conditions and `target` cannot be used simultaneously.

### Defining tags

Versioned migration scripts can be given tags by specifying [tag definitions](<Configuration/Flyway Namespace/Flyway Tags Namespace/Flyway Tags Definitions Setting>)
inside the project toml. 

Example:

```toml
[flyway.tags.definitions]
"0.1" = ["tagA"]
"0.2" = ["tagB"]
"0.3" = ["tagC", "tagB"]
```

Repeatable migrations are not supported with tags at this time.

### Selecting migrations for deployment using tag conditions

Migrations can be selected by tag for use with flyway commands such as `migrate` and `undo` using the `anyOf`, 
`allOf` and `noneOf` conditions. If more than one tag condition is specified, then migrations must match the 
criteria of all given conditions to be selected. In the case where no migrations match the given tag conditions, 
flyway will raise an error.

Example:
```powershell
./flyway "-tags.anyOf=tagB" "-tags.noneOf=tagC" info
```

With the toml specified above this would return only migration `0.3` as `pending` from `info`, with `0.1` and `0.2` 
marked as `ignored`.

### Controlling deployment order using tag conditions

Tags can be used to control the order migrations are run using the [deployment order](<Configuration/Flyway Namespace/Flyway Tags Namespace/Flyway Tags Deployment Order Setting>).

Example:
```powershell
./flyway "-tags.deploymentOrder=tagB,tagA" info
```

With the toml specified above this would select migrations in the order `0.2`, `0.3`, `0.1`, rather than the default 
behavior of running migrations in version order.