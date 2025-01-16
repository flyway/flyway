---
pill: schemaModelSchemas
subtitle: flyway.schemaModelSchemas
redirect_from: Configuration/schemaModelSchemas/
---

## Description

An array of schemas that should be supported by the schema model folder.
It is analogous to the [schemas](<Configuration/Environments Namespace/Environment Schemas Setting>) property for an environment.

When using `schemaModel` as a comparison source or target with the `diff` or
`prepare` commands, this allows for mapping source schemas to target schemas when they do not match.

## Type

String array

## Default

If this is not set, then schema model schemas will be assumed to match the other side of the comparison when running
`diff` or `prepare` commands.

## Usage

### Flyway Desktop

In Flyway Desktop the schema model schemas are assumed to match the development database schemas.

### Command-line

```bash
./flyway -schemaModelSchemas="schema1,schema2"
```

### TOML Configuration File

```toml
[flyway]
schemaModelSchemas = [ "schema1", "schema2" ]
```
