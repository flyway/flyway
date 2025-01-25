---
pill: schemaModelSchemas
subtitle: flyway.schemaModelSchemas
redirect_from: Configuration/schemaModelSchemas/
---

# Schema Model Schemas

## Description
A list of schemas that should be supported by the schema model folder.
It is analogous to the [schemas](<Configuration/Parameters/Environments/Schemas>) property for an environment.

This option may be required when using `schemaModel` as a comparison source or target with the `diff` command.

It may also be required when using a database that supports schema mapping. For example, consider that the schema model
has a reference to the schema "schema_dev" and the shadow database uses the schema name "schema_shadow". The
`schemaModelSchemas` option is required when performing a `diff` operation between those two sources, so that the `diff`
command is aware they represent the same schema instead of being two separate schemas.

## Usage

### Commandline
```bash
./flyway -schemaModelSchemas="schema1,schema2"
```

### TOML
```properties
[flyway]
schemaModelSchemas=[ "schema1", "schema2" ]
```
