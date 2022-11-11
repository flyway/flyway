---
layout: documentation
menu: tut_injectingenvironments
subtitle: 'Tutorial: Injecting Environments'
---
# Tutorial: Injecting Environments
{% include teams.html %}

This brief tutorial will teach you **how to inject environments for your migrations to execute against**.

## Introduction

When working with databases you often have different environments such as development, test or production. In each of these environments you might want to execute different migrations, and this can be achieved with a placeholder and the `shouldExecute` script configuration parameter.

`shouldExecute` is a parameter that lets you easily customize when a migration should execute by providing it a boolean expression. Unlike [`skipExecutingMigrations`](/documentation/configuration/parameters/skipExecutingMigrations), this will **not** update the schema history table. It also supports placeholders within its boolean expressions, which gives you the flexibility to customize the execution of migrations as needed.

If you aren’t already familiar with the concept of script configuration parameters, you can read about it [here](/documentation/configuration/scriptconfigfiles). If you’d like to brush up on your knowledge of placeholders, you can read about them [here](/documentation/configuration/placeholder).

## Example

Let’s say we have the following migrations:

```
V1__dev_migration_1.sql
V2__tst_migration_1.sql
V3__prd_migration_1.sql
```

`V1` should only be executed in the `development` environment, `V2` in the `test` environment and `V3` in the `production` environment.

Migration `V1`’s script configuration file `V1__dev_migration_1.sql.conf` will need the line `shouldExecute=${environment}==development`.<br/>
Migration `V2`’s script configuration file `V2__tst_migration_1.sql.conf` will need the line `shouldExecute=${environment}==test`.<br/>
Migration `V3`’s script configuration file `V3__prd_migration_1.sql.conf` will need the line `shouldExecute=${environment}==production`.

If we set the value of the `${environment}` placeholder to contain the environment we are running Flyway in, we can achieve our desired result.

Running:

`flyway -placeholders.environment=development migrate`

Will only apply `V1`. Similarly, running:

`flyway -placeholders.environment=test migrate`

Will only apply `V2` and running:

`flyway -placeholders.environment=production migrate`

Will only apply `V3`.

## Summary

In this brief tutorial we saw how to:

- Use `shouldExecute` to control which environments our migrations execute in
