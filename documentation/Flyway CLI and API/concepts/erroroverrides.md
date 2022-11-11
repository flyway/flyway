---
layout: documentation
menu: erroroverrides
subtitle: Error Overrides
redirect_from: /documentation/erroroverrides/
---
# Error Overrides
{% include teams.html %}

When Flyway executes SQL statements it reports all warnings returned by the database. In case an error is returned
Flyway displays it with all necessary details, marks the migration as failed and automatically rolls it back if possible.

The error usually looks like this:

```
Migration V1__Create_person_table.sql failed
--------------------------------------------
SQL State  : 42001
Error Code : 42001
Message    : Syntax error in SQL statement "CREATE TABLE1[*] PERSON "; expected "OR, FORCE, VIEW, ...
Location   : V1__Create_person_table.sql (/flyway-tutorial/V1__Create_person_table.sql)
Line       : 1
Statement  : create table1 PERSON
```

This default behavior is great for the vast majority of the cases.
 
There are however situations where you may want to
- treat an error as a warning as you know your migration will handle it correctly later
- treat a warning as an error as you prefer to fail fast to be able to fix the problem sooner
- perform an additional action when a specific error or warning is being emitted by the database

Flyway Teams Edition give you a way to achieve all these scenarios using **Error Overrides**.

## Configuration

One or more Errors Overrides can be configured using the [`errorOverrides`](/documentation/configuration/parameters/errorOverrides)
setting which accepts multiple error override definitions in the following form: `STATE:12345:W`.
                             
This is a 5 character SQL state, a colon, the SQL error code, a colon and finally the desired
behavior that should override the initial one. The following behaviors are accepted:
- `D` to force a debug message
- `D-` to force a debug message, but do not show the original sql state and error code
- `I` to force an info message
- `I-` to force an info message, but do not show the original sql state and error code
- `W` to force a warning
- `W-` to force a warning, but do not show the original sql state and error code
- `E` to force an error
- `E-` to force an error, but do not show the original sql state and error code
              
If no matching Error Overrides are configured Flyway falls back to its default behavior.

## Examples

Here are some examples on how to use this feature.

### Example 1: Throw an error when Oracle stored procedure compilation fails

By default when an Oracle stored procedure compilation fails, the driver simply returns a warning which is being output
by Flyway as

```
DB: Warning: execution completed with warning (SQL State: 99999 - Error Code: 17110)
```

To force Oracle stored procedure compilation issues to produce
errors instead of warnings, all one needs to do is add the following to Flyway's configuration:
 
```properties
flyway.errorOverrides=99999:17110:E
```

All Oracle stored procedure compilation failures will then result in an **immediate error**.

### Example 2: Display SQL Server PRINT messages as simple info messages

By default when a SQL Server `PRINT` statement executes, the message is returned as a warning to the client. This
means that the following statements:

```sql
PRINT 'Starting ...';
PRINT 'Done.';
```

produce the following output by default:

```
WARNING: DB: Starting ... (SQL State: S0001 - Error Code: 0)
WARNING: DB: Done. (SQL State: S0001 - Error Code: 0)
```

To force these `PRINT` statements to produce simple info messages (with no SQL State and Error Code details) instead
of warnings, all one needs to do is add the following to Flyway's configuration:

```properties
flyway.errorOverrides=S0001:0:I-
```

With that setting in place the output then simply becomes **info messages with no SQL State and Error Code details**:

```
Starting ...
Done.
```

## Advanced programmatic configuration

As an alternative to the simple declarative syntax presented above, you can also fully customize the behavior of Flyway
following the execution of a statement by implementing a Java-based [callback](/documentation/concepts/callbacks) which listens
to the `afterEachMigrateStatement`, `afterEachMigrateStatementError`, `afterEachUndoStatement` and
`afterEachUndoStatementError` events.

## Tutorial

Click [here](/documentation/getstarted/advanced/erroroverrides) to see a tutorial on using error overrides.

<p class="next-steps">
    <a class="btn btn-primary" href="/documentation/concepts/dryruns">Dry Runs <i class="fa fa-arrow-right"></i></a>
</p>