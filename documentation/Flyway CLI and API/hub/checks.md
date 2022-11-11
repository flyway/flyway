---
layout: documentation
menu: hub_checks
subtitle: Migration script checks with Flyway Hub
redirect_from: /documentation/hub-checks
---

# Introducing Migration script checks

We're calling all those tasks that should be completed before a database deployment happens "Migration script checks". With Flyway Hub we are introducing the first of those checks, **Build** - with many more to come.

## Build

The Build check will test if your migration scripts can be run against a real, empty database. This has many benefits including:

- Validating syntax of all scripts
- Checking migration script order
- Highlighting version number conflicts
- Ensuring new environments can be built from source control

This simple but effective check is often avoided due to having to provision infrastructure and/or write code to set up and tear down the test. Flyway Hub handles this for you, allowing the check to be run easily with one click.

A build runs through the following steps:

- Create a dedicated, temporary database instance with an empty database
- Check out your repository from GitHub
- Run a `flyway migrate` command using your migrations folder and the temporary database instance
- Collect the results
- Delete the database instance

The provisioning of the instance typically takes ~30 seconds with the remaining time depending on the number and size of migrations in your project.

When creating a new project in Flyway Hub, we will do our best to automatically detect settings from your repository. However on some occasions we may need you to supply the following:

- Migrations folder - The folder in your repository where your migration scripts are located
- Database engine - The engine to use when running the check

Once you have a project created, you can easily <a href="/documentation/hub/automation">automate</a> the checks.

<a href="/documentation/hub/commandline"
        class="btn btn-primary">Command line <i class="fa fa-arrow-right"></i></a>
