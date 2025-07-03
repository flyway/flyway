---
subtitle: flyway.generate.force
---

## Description

Whether to use placeholders in the generated migration script. If set to `true`, Flyway will attempt to insert placeholders into the script.

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

### Evaluation order
Placeholders are evaluated in order of longest value first. This means that if you have these placeholders:

```bash
flyway generate -generate.usePlaceholders=true -placeholders.foo=local -placeholders.bar=localhost
```

Any text matching `localhost` will be replaced with `${bar}`, and then any remaining text matching `local` will be replaced with `${foo}`.


### Limitations

If multiple placeholders are specified with the same value, Flyway will throw an exception. For example, if you specify `-placeholders.schema=dbo -placeholders.x=dbo`, Flyway will throw an error.

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
