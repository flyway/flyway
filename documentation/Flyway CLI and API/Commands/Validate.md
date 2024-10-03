---
subtitle: Validate
---
# Validate

Validates the applied migrations against the available ones.

![Validate](assets/command-validate.png)

Validate helps you verify that the migrations applied to the database match the ones available locally.

This is very useful to detect accidental changes that may prevent you from reliably recreating the schema.

Validate works by storing a checksum (CRC32 for SQL migrations) when a migration is executed. The validate mechanism checks if the migration locally still has the same checksum as the migration already executed in the database.

## Custom validation rules

As the lifetime of a project increases, there will inevitably be hotfixes, deleted migrations and other changes that break the conventions of Flyway's validation.

In these cases you need a way to tell Flyway that these migrations are valid. Flyway Teams Edition provides the flexibility to do this.

<a style="text-decoration: none; background: rgb(204,0,0); padding: 6px 40px; border-radius: 10px; color: white; font-weight: bold;" href="https://www.red-gate.com/blog/customize-validation-rules-with-ignoremigrationpatterns">Learn more about custom validate rules</a>

## Usage
See [configuration](Configuration/parameters/#validate) for validate specific configuration parameters.
{% include commandUsage.html command="Validate" %}
