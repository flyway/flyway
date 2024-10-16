---
subtitle: Callbacks
redirect_from: /documentation/callbacks/
---

# Callbacks

While migrations are sufficient for most needs, there are certain situations that require you to <strong>execute the same action
over and over again</strong>. This could be recompiling procedures, updating materialized views and many other types of housekeeping.

For this reason, Flyway offers you the possibility to **hook into its lifecycle** by using Callbacks.
In order to use these callbacks, name a script after the callback name (ie. afterMigrate.sql) and drop it alongside your other scripts in your migrations folder. Flyway will then invoke it when the execution event criteria listed below is met.

These are the events Flyway supports:
<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Migrate</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="beforeMigrate">
        <td>beforeMigrate</td>
        <td>Before Migrate runs</td>
    </tr>
    <tr id="beforeRepeatables">
        <td>beforeRepeatables</td>
        <td>Before all repeatable migrations during Migrate</td>
    </tr>
    <tr id="beforeEachMigrate">
        <td>beforeEachMigrate</td>
        <td>Before every single migration during Migrate</td>
    </tr>
    <tr id="beforeEachMigrateStatement">
        <td>beforeEachMigrateStatement</td>
        <td>Before every single statement of a migration during Migrate</td>
    </tr>
    <tr id="afterEachMigrateStatement">
        <td>afterEachMigrateStatement</td>
        <td>After every single successful statement of a migration during Migrate</td>
    </tr>
    <tr id="afterEachMigrateStatementError">
        <td>afterEachMigrateStatementError</td>
        <td>After every single failed statement of a migration during Migrate</td>
    </tr>
    <tr id="afterEachMigrate">
        <td>afterEachMigrate</td>
        <td>After every single successful migration during Migrate</td>
    </tr>
    <tr id="afterEachMigrateError">
        <td>afterEachMigrateError</td>
        <td>After every single failed migration during Migrate</td>
    </tr>
    <tr id="afterMigrate">
        <td>afterMigrate</td>
        <td>After successful Migrate runs</td>
    </tr>
    <tr id="afterMigrateApplied">
        <td>afterMigrateApplied</td>
        <td>After successful Migrate runs where at least one migration has been applied</td>
    </tr>
    <tr id="afterVersioned">
        <td>afterVersioned</td>
        <td>After all versioned migrations during Migrate</td>
    </tr>
    <tr id="afterMigrateError">
        <td>afterMigrateError</td>
        <td>After failed Migrate runs</td>
    </tr>
    </tbody>
</table>

<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Undo</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="beforeUndo">
        <td>beforeUndo <br>{% include teams.html %}</td>
        <td>Before Undo runs</td>
    </tr>
    <tr id="beforeEachUndo">
        <td>beforeEachUndo <br>{% include teams.html %}</td>
        <td>Before every single migration during Undo</td>
    </tr>
    <tr id="beforeEachUndoStatement">
        <td>beforeEachUndoStatement <br>{% include teams.html %}</td>
        <td>Before every single statement of a migration during Undo</td>
    </tr>
    <tr id="afterEachUndoStatement">
        <td>afterEachUndoStatement <br>{% include teams.html %}</td>
        <td>After every single successful statement of a migration during Undo</td>
    </tr>
    <tr id="afterEachUndoStatementError">
        <td>afterEachUndoStatementError <br>{% include teams.html %}</td>
        <td>After every single failed statement of a migration during Undo</td>
    </tr>
    <tr id="afterEachUndo">
        <td>afterEachUndo <br>{% include teams.html %}</td>
        <td>After every single successful migration during Undo</td>
    </tr>
    <tr id="afterEachUndoError">
        <td>afterEachUndoError <br>{% include teams.html %}</td>
        <td>After every single failed migration during Undo</td>
    </tr>
    <tr id="afterUndo">
        <td>afterUndo <br>{% include teams.html %}</td>
        <td>After successful Undo runs</td>
    </tr>
    <tr id="afterUndoError">
        <td>afterUndoError <br>{% include teams.html %}</td>
        <td>After failed Undo runs</td>
    </tr>
    </tbody>
</table>
	
<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Clean</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>	
	
    <tr id="beforeClean">
        <td>beforeClean</td>
        <td>Before Clean runs</td>
    </tr>
    <tr id="afterClean">
        <td>afterClean</td>
        <td>After successful Clean runs</td>
    </tr>
    <tr id="afterCleanError">
        <td>afterCleanError</td>
        <td>After failed Clean runs</td>
    </tr>
    </tbody>
</table>

<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Info</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="beforeInfo">
        <td>beforeInfo</td>
        <td>Before Info runs</td>
    </tr>
    <tr id="afterInfo">
        <td>afterInfo</td>
        <td>After successful Info runs</td>
    </tr>
    <tr id="afterInfoError">
        <td>afterInfoError</td>
        <td>After failed Info runs</td>
    </tr>
    </tbody>
</table>

_Note: It is strongly discouraged to include any write-related callbacks for the Info command. 
The Info command may be internally triggered by Flyway._

<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Validate</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="beforeValidate">
        <td>beforeValidate</td>
        <td>Before Validate runs</td>
    </tr>
    <tr id="afterValidate">
        <td>afterValidate</td>
        <td>After successful Validate runs</td>
    </tr>
    <tr id="afterValidateError">
        <td>afterValidateError</td>
        <td>After failed Validate runs</td>
    </tr>
    </tbody>
</table>

<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Baseline</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="beforeBaseline">
        <td>beforeBaseline</td>
        <td>Before Baseline runs</td>
    </tr>
    <tr id="afterBaseline">
        <td>afterBaseline</td>
        <td>After successful Baseline runs</td>
    </tr>
    <tr id="afterBaselineError">
        <td>afterBaselineError</td>
        <td>After failed Baseline runs</td>
    </tr>
    </tbody>
</table>

<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Repair</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="beforeRepair">
        <td>beforeRepair</td>
        <td>Before Repair runs</td>
    </tr>
    <tr id="afterRepair">
        <td>afterRepair</td>
        <td>After successful Repair runs</td>
    </tr>
    <tr id="afterRepairError">
        <td>afterRepairError</td>
        <td>After failed Repair runs</td>
    </tr>
    </tbody>
</table>

<table class="table table-hover">
    <thead>
    <tr>
        <th><strong>Name</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="createSchema">
        <td>createSchema [deprecated, use beforeCreateSchema]</td>
        <td>Before automatically creating non-existent schemas</td>
    </tr>
    <tr id="beforeCreateSchema">
        <td>beforeCreateSchema</td>
        <td>Before automatically creating non-existent schemas</td>
    </tr>
    <tr id="beforeConnect">
        <td>beforeConnect {% include redgate.html %}</td>
        <td>Before Flyway connects to the database</td>
    </tr>
    </tbody>
</table>

Callbacks can be implemented either in SQL or in Java.

## Location
Flyway will discover SQL callbacks in the same manner as SQL migration scripts, based on the [locations](Configuration/parameters/flyway/locations) defined.

Whereas Java callbacks will be picked up from the classpath.

## SQL Callbacks

The most convenient way to hook into Flyway's lifecycle is through SQL callbacks. These are simply sql files
in the configured locations following a certain naming convention: the event name followed by the SQL migration suffix.

Placeholder replacement works just like it does for <a href="Concepts/migrations#sql-based-migrations">SQL migrations</a>.

Optionally the callback may also include a description. In that case the callback name is composed of the event name,
the separator, the description and the suffix. Example: `beforeRepair__vacuum.sql`.

**Note:** Flyway will also honor any `sqlMigrationSuffixes` you have configured, when scanning for SQL callbacks.

## Java Callbacks

If SQL Callbacks aren't flexible enough for you, you have the option to implement the
[**Callback**](https://javadoc.io/doc/org.flywaydb/flyway-core/latest/org/flywaydb/core/api/callback/Callback.html)
interface yourself. You can even hook multiple Callback implementations in the lifecycle. Java callbacks have the
additional flexibility that a single Callback implementation can handle multiple lifecycle events, and are
therefore not bound by the SQL callback naming convention.

**More info:** [Java-based Callbacks](usage/API (Java)/API - hooks#callsbacks)

## Script Callbacks

Much like SQL callbacks, Flyway also supports the execution of callbacks written in a scripting language. The supported file extensions are the same as those supported by [script migrations](concepts/migrations#Migrations-Scriptmigrations). For example, you could have a `beforeRepair__vacuum.ps1` callback. Script callbacks give you much more flexibility and power during the migration lifecycle. Some of the things you can achieve are:

- Executing external tools between migrations
- Creating or cleaning local files for migrations to use

## Callback ordering

When multiple callbacks for the same event are found, they are executed in the alphanumerical order. 
E.G.
```sql
beforeConnect__1.sql
beforeConnect__2.sql
beforeConnect__a.sql
beforeConnect__b.sql
```

## Tutorial

Click [here](Tutorials/Tutorial Callbacks) to see a tutorial on using callbacks.
