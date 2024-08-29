---
subtitle: Should Execute 
---
# Should Execute
{% include teams.html %}

## Description
`shouldExecute` is a script configuration option that lets you define when a migration should execute. 
Unlike [skipExecutingMigrations](<configuration/parameters/flyway/Skip Executing Migrations>), this will _not_ update the 
schema history table.

## Usage
In order to use this option, you first have to create a [script configuration](<Configuration/Script Configuration>) file which defines the conditions under which the script should be executed.
This file contains the options for this migration script

### Configuration File

```properties
shouldExecute=expression
```

Where `expression` evaluates to a boolean value
- Either `true` or `false`
- `A==B` or `A!=B` where `A` and `B` are themselves values and not expressions
-  Combinations of these using `&&` (AND), `||` (OR) and parentheses `( )`

Flyway will change behavior for this migration depending on the expression evaluation:
- If it evaluates to true, this migration is executed
- If it evaluates to false, this migration is ignored
- If it fails to evaluate, an exception will be thrown
 
The power of this configuration comes from their integration with [placeholders](Configuration/parameters/flyway/placeholders) that allow you to set the condition for a specific script and control that condition globally.

For examples of how to use this feature, see the [shouldExecute Concept](<Concepts/Should Execute Concept>) page 
