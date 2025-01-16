---
subtitle: Should Execute 
---

{% include teams.html %}

## Description

`shouldExecute` is a script configuration option that lets you define when a migration should execute.

This consists of an expression which evaluates to a Boolean value. Flyway will change behavior for this migration depending on the expression evaluation:
- If it evaluates to true, this migration is executed
- If it evaluates to false, this migration is ignored
- If it fails to evaluate, an error will be returned

Unlike [skipExecutingMigrations](<Configuration/Flyway Namespace/Flyway Skip Executing Migrations Setting>), this will _not_ update the schema history table when a script is not executed.

## Valid values

- Either `true` or `false`
- `<A>==<B>` or `<A>!=<B>` where `<A>` and `<B>` are  [placeholders](<Configuration/Flyway Namespace/Flyway Placeholders Namespace>) or literal values.
- Combinations of these using `&&` (AND), `||` (OR) and parentheses `( )`

## Default

`true`

## Usage

### Script configuration File

```properties
shouldExecute=${flyway:defaultSchema}==A
``` 
