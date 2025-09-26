---
subtitle: flyway.generate.force
---

## Description

Whether to create placeholders in the generated migration script. If set to `true`, Flyway will attempt to insert placeholders into the script.

These placeholders can then be used when you deploy the script using Flyway [placeholders](<configuration/flyway-namespace/flyway placeholders namespace>) replacement mechanism. This results in an abstraction of the generated script away from specific database parameter values (for example the database instance or schema names). 

For example, if `flyway generate <...>` would create the following SQL:

```sql
CREATE TABLE dbo.foo (
    id INT NOT NULL,
    name VARCHAR(255) NOT NULL
);
```

`flyway generate -generate.usePlaceholders=true -placeholders.schema=dbo <...>` would create:

```sql
CREATE TABLE ${schema}.foo (
    id INT NOT NULL,
    name VARCHAR(255) NOT NULL
);
```

Please note - this is doing a text pattern match for the string `"dbo"` and replacing it with the string `"${schema}"`, this is what `-placeholders.schema=dbo` defines. It isn't aware of SQL syntax or your database !

### Evaluation order
Placeholders are evaluated in order of longest value first. This means that if you have these placeholders:

```bash
flyway generate -generate.usePlaceholders=true -placeholders.foo=local -placeholders.bar=localhost
```

Any text matching the string `"localhost"` will be replaced with the string `"${bar}"`, and then any remaining text matching `"local"` will be replaced with `"${foo}"`.


### Limitations

If multiple placeholders are specified with the same value, Flyway will throw an exception. For example, if you specify `-placeholders.schema=dbo -placeholders.x=dbo`, Flyway will throw an error.

The reason being that you are asking Flyway to replace the string `"dbo"` with both `"${schema}"` and `"${x}"` which can't be done.

## Type

Boolean

## Default

`false`

## Usage

### Flyway Desktop

This can't be added to a configuration file via Flyway Desktop.

### Command-line

```bash
./flyway generate -usePlaceholders=true
```

### TOML Configuration File

```toml
[flyway.generate]
usePlaceholders = true
```
