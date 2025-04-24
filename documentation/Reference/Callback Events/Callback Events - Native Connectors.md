---
subtitle: Callbacks - Native Connectors
---

This page lists the callback events that are compatible with Native Connectors.

## List of events

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
    <tr id="beforeEachMigrate">
        <td>beforeEachMigrate</td>
        <td>Before every single migration during Migrate</td>
    </tr>
    <tr id="afterEachMigrate">
        <td>afterEachMigrate</td>
        <td>After every single successful migration during Migrate</td>
    </tr>
    <tr id="afterMigrate">
        <td>afterMigrate</td>
        <td>After successful Migrate runs</td>
    </tr>
    <tr id="afterMigrateApplied">
        <td>afterMigrateApplied</td>
        <td>After successful Migrate runs where at least one migration has been applied</td>
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
        <th><strong>Name</strong></th>
        <th><strong>Execution</strong></th>
    </tr>
    </thead>
    <tbody>
    <tr id="afterConnect">
        <td>afterConnect</td>
        <td>Immediately after Flyway connects to the database</td>
    </tr>
    </tbody>
</table>

